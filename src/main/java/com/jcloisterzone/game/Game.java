package com.jcloisterzone.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.ai.AiPlayerAdapter;
import com.jcloisterzone.ai.ForceSupportIfSupports;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.ClockUpdateEvent;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.GameOverEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderState;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;
import com.jcloisterzone.wsio.message.WsSaltMeesage;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Queue;


/**
 * Other information than board needs in game. Contains players with their
 * points, followers ... and game rules of current game.
 */
//TODO remove extends from GameSettings
public class Game implements EventProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Connection connection;

    private final String gameId;
    private String name;
    private long initialSeed;

    private GameSetup setup;
    private GameState state;
    private Array<PlayerClock> clocks;
    private GameStatePhaseReducer phaseReducer;
    private List<WsReplayableMessage> replay; // game messages (in reversed order because of List performance)
    private java.util.HashMap<String, Object> gameAnnotations;

    protected PlayerSlot[] slots;
    protected SupportedSetup[] slotSupported = new SupportedSetup[PlayerSlot.COUNT];
    private boolean aiPlayersRegistered;

    private List<UndoHistoryItem> undoHistory = List.empty();

    private final EventBus eventBus = new EventBus(new EventBusExceptionHandler("game event bus"));

    private int idSequenceCurrVal = 0;
    /** message counter in current phase */
    private int messageIdPhaseSequence = 0;

    public Game(String gameId, long randomSeed) {
        this.gameId = gameId;
        this.initialSeed = randomSeed;
    }

    public String getGameId() {
        return gameId;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInitialSeed() {
        return initialSeed;
    }

    public GameState getState() {
        return state;
    }

    public void setSetup(GameSetup setup) {
        this.setup = setup;
    }

    public GameSetup getSetup() {
        return setup;
    }

    public Array<PlayerClock> getClocks() {
        return clocks;
    }

    public void mapSetup(Function<GameSetup, GameSetup> mapper) {
        setSetup(mapper.apply(setup));
    }

    public List<WsReplayableMessage> getReplay() {
        return replay;
    }

    public void replaceState(GameState state) {
        if (this.state == state) {
            return;
        }

        GameState prev = this.state;
        this.state = state;

        Player player = state.getActivePlayer();
        if (player != null && !player.equals(prev.getActivePlayer()) && player.getSlot().isOwn()) {
            connection.send(new ToggleClockMessage(player.getIndex()));
        }

        boolean gameIsOver = isOver();
        if (gameIsOver) {
            if (state.getTurnPlayer().getSlot().isOwn()) { // send messages only from one client
                connection.send(new ToggleClockMessage(null));
                connection.send(new GameOverMessage());
            }
        }

        GameChangedEvent ev = new GameChangedEvent(prev, state);
        post(ev);

        if (gameIsOver) {
            post(new GameOverEvent());
        }

        if (logger.isDebugEnabled()) {
            StringBuilder sb;
            Queue<PlayEvent> playEvents = ev.getNewPlayEvents();
            if (!playEvents.isEmpty()) {
                sb = new StringBuilder();
                sb.append("play events:");
                for (PlayEvent pev : ev.getNewPlayEvents()) {
                    sb.append("\n  - ");
                    sb.append(pev.toString());
                }
                logger.debug(sb.toString());
            }

            ActionsState as = state.getPlayerActions();
            if (as != null) {
                sb = new StringBuilder();
                sb.append(as.getPlayer().getNick());
                sb.append("'s actions:");
                for (PlayerAction<?> action : as.getActions()) {
                    sb.append("\n  - ");
                    sb.append(action.toString());
                    if (action.getOptions() != null) { // bazaar actions can be empty, handled in different way
                        sb.append("\n    ");
                        sb.append(String.join(", " , action.getOptions().map(Object::toString)));
                    }
                }
                logger.debug(sb.toString());
            }
        }
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    private void markUndo() {
        undoHistory = undoHistory.prepend(
            new UndoHistoryItem(state, replay)
        );
    }

    private void clearUndo() {
        undoHistory = List.empty();
    }

    public List<UndoHistoryItem> getUndoHistory() {
        return undoHistory;
    }

    public boolean isUndoAllowed() {
        return !undoHistory.isEmpty();
    }

    public void undo() {
        if (undoHistory.isEmpty()) {
            throw new IllegalStateException();
        }
        Tuple2<UndoHistoryItem, List<UndoHistoryItem>> head = undoHistory.pop2();
        undoHistory = head._2;
        replay = head._1.getReplay();
        // note: seed should be unchanged for current usage
        // when seed is changed undo history is cleared
        replaceState(head._1.getState());
    }

    public String getMessageId() {
        return getMessageId(state);
    }

    private String getMessageId(GameState state) {
        String extra = BuilderState.SECOND_TURN == state.getCapabilityModel(BuilderCapability.class) ? "*" : "";
        return String.format("%s%s.%s.%s", state.getTurnNumber(), extra, state.getPhase().getSimpleName(), messageIdPhaseSequence);
    }

    @WsSubscribe
    public void handleSlotMessage(SlotMessage msg) {
        slotSupported[msg.getNumber()] = msg.getSupportedSetup();
        post(new SupportedExpansionsChangeEvent(mergeSupportedExpansions()));
    }

    @WsSubscribe
    public void handleInGameMessage(WsReplayableMessage msg) {
        if (!replay.isEmpty()) {
            WsReplayableMessage prevMsg = replay.get();
            if (prevMsg.getMessageId().equals(msg.getMessageId())) {
                logger.warn("Dropping already delivered message {}", msg.getMessageId());
                return;
            }
        }
        // first, try to derive new state to verify that message is valid
        long origSalt = phaseReducer.getRandom().getSalt();
        GameState newState = null;
        try {
            if (msg instanceof WsSaltMeesage) {
                phaseReducer.getRandom().setSalt(((WsSaltMeesage)msg).getSalt());
            }
            newState = phaseReducer.apply(state, msg);
        } catch (Exception e) {
            // revert salt back as message never received
            phaseReducer.getRandom().setSalt(origSalt);
            throw e;
        }

        updateMessageIdPhaseSequence(state, newState);
        markUndo();
        replay = replay.prepend(msg);

        Player activePlayer = newState.getActivePlayer();
        if (msg instanceof WsSaltMeesage
            || activePlayer == null
            || !activePlayer.equals(undoHistory.get().getState().getActivePlayer())
        ) {
            clearUndo();
        }
        replaceState(newState);

    }

    private void updateMessageIdPhaseSequence(GameState oldState, GameState newState) {
        if (oldState.getPhase().equals(newState.getPhase())) {
            messageIdPhaseSequence++;
        } else {
            messageIdPhaseSequence = 0;
        }
    }

    @WsSubscribe
    public void handleClockMessage(ClockMessage msg) {
        long[] clockValues = msg.getClocks();
        for (int i = 0; i < clockValues.length; i++) {
            boolean running = msg.getRunning() != null && msg.getRunning() == i;
            PlayerClock clock = clocks.get(i);
            PlayerClock newClock = clock.setRunning(running).setTime(clockValues[i]);
            if (clock != newClock) {
                clocks = clocks.update(i, newClock);
            }
        }
        post(new ClockUpdateEvent(clocks, msg.getRunning()));
    }


    private java.util.Set<Expansion> mergeSupportedExpansions() {
        SupportedSetup merged = SupportedSetup.getCurrentClientSupported();
        for (int i = 0; i < slotSupported.length; i++) {
            if (slotSupported[i] == null) {
                continue;
            }
            merged = merged.intersect(slotSupported[i]);
        }

        java.util.Set<Expansion> supportedExpansions = new HashSet<>();
        outer:
        for (Expansion exp : Expansion.values()) {
            if (!merged.getTiles().contains(exp)) {
                continue outer;
            }
            for (Class<? extends Capability<?>> cap : exp.getCapabilities()) {
                if (merged.getCapabilities().contains(cap)) {
                    continue;
                }
                ForceSupportIfSupports forced = cap.getAnnotation(ForceSupportIfSupports.class);
                if (forced != null && merged.getCapabilities().contains(forced.value())) {
                    continue;
                }
                continue outer;
            }
            supportedExpansions.add(exp);
        }
        return supportedExpansions;
    }

    @Override
    public void post(Event event) {
        eventBus.post(event);
    }

    public void start(GameController gc, List<WsReplayableMessage> replay, HashMap<String, Object> savedGameAnnotations) {
        this.replay = replay.reverse();
        phaseReducer = new GameStatePhaseReducer(setup, initialSeed);
        GameStateBuilder builder = new GameStateBuilder(setup, slots, gc.getConfig());
        if (savedGameAnnotations != null) {
            gameAnnotations = savedGameAnnotations;
        } else {
            DebugConfig debugConfig = gc.getConfig().getDebug();
            if (debugConfig != null) {
                gameAnnotations = debugConfig.getGame_annotation();
            }
        }
        builder.setGameAnnotations(gameAnnotations);

        // 1. create state with basic config
        GameState state = builder.createInitialState();
        this.state = state; // set state to get proper state diff against empty state later (in replacedState)
        clocks = state.getPlayers().getPlayers().map(p -> new PlayerClock(0));

        // 2. Register local AI players
        createAiPlayers(gc);

        // 3. notify started game - event handlers requires initial state with game config to be set
        gc.onGameStarted(this);

        // 4. trigger initial board changes - make it after started event to propagate all event correctly to GameView
        Phase firstPhase = phaseReducer.getFirstPhase();
        state = builder.createReadyState(state);
        state = state.setPhase(firstPhase.getClass());
        state = phaseReducer.applyStepResult(firstPhase.enter(state));
        for (WsReplayableMessage msg : replay) {
            msg.setMessageId(getMessageId(state));
            if (msg instanceof WsSaltMeesage) {
                phaseReducer.getRandom().setSalt(((WsSaltMeesage) msg).getSalt());
            }
            GameState prev = state;
            state = phaseReducer.apply(state, msg);
            updateMessageIdPhaseSequence(prev, state);
        }
        replaceState(state);
    }

    private void createAiPlayers(GameController gc) {
        if (aiPlayersRegistered) {
            // do not create AI players for second time when game is just resumed
            // eg. reconnect after lost connection or simply continue paused game on play server
            return;
        }
        for (PlayerSlot slot : slots) {
            if (slot != null && slot.isAi() && slot.isOwn()) {
                try {
                    AiPlayer ai = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                    for (Player player : this.state.getPlayers().getPlayers()) {
                        if (player.getSlot().getNumber() == slot.getNumber()) {
                            AiPlayerAdapter adapter = new AiPlayerAdapter(gc, player, ai);
                            eventBus.register(adapter);
                            logger.info("AI player created - " + slot.getAiClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Unable to create AI player", e);
                }
            }
        }
        aiPlayersRegistered = true;
    }

    public void setSlots(PlayerSlot[] slots) {
        this.slots = slots;
    }

    public PlayerSlot[] getPlayerSlots() {
        return slots;
    }

    public Phase getPhase() {
        if (state == null) {
            return null;
        }
        return phaseReducer.getPhase(state.getPhase());
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return state.getDeployedMeeples();
    }

    public Meeple getMeeple(MeeplePointer mp) {
        Tuple2<Meeple, FeaturePointer> match =
            getDeployedMeeples().find(t -> mp.match(t._1)).getOrNull();
        return match == null ? null : match._1;
    }

    public boolean isStarted() {
        return state != null;
    }

    public boolean isOver() {
        return getPhase() instanceof GameOverPhase;
    }

    public int idSequnceNextVal() {
        return ++idSequenceCurrVal;
    }

    public java.util.HashMap<String, Object> getGameAnnotations() {
        return gameAnnotations;
    }
}
