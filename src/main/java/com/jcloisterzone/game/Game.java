package com.jcloisterzone.game;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
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
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.ClockUpdateEvent;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.GameOverEvent;
import com.jcloisterzone.event.GameStartedEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WebSocketConnection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;
import com.jcloisterzone.wsio.message.WsSeedMeesage;

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

    private WebSocketConnection connection;

    private final String gameId;
    private String name;
    private long initialSeed;

    private GameSetup setup;
    private GameState state;
    private Array<PlayerClock> clocks;
    private GameStatePhaseReducer phaseReducer;
    private List<WsReplayableMessage> replay; // game messages (in reversed order because of List performance)


    protected PlayerSlot[] slots;
    protected Expansion[][] slotSupportedExpansions = new Expansion[PlayerSlot.COUNT][];

    private List<GameState> undoState = List.empty();

    private final EventBus eventBus = new EventBus(new EventBusExceptionHandler("game event bus"));
    //events are delayed and fired after phase is handled (and eventually switched to the new one) - important especially for AI handlers to not start before switch is done
    //private final java.util.Deque<Event> eventQueue = new java.util.ArrayDeque<>();

    private int idSequenceCurrVal = 0;

    private final Random random;

//    public Game(String gameId) {
//        this(gameId, HashCode.fromBytes(gameId.getBytes()).asLong());
//    }

    public Game(String gameId, long randomSeed) {
        this.gameId = gameId;
        this.initialSeed = randomSeed;
        this.random = new Random(randomSeed);
    }

    public String getGameId() {
        return gameId;
    }

    public WebSocketConnection getConnection() {
        return connection;
    }

    public void setConnection(WebSocketConnection connection) {
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
        GameState prev = this.state;
        this.state = state;

        Player player = state.getActivePlayer();
        if (player != null && !player.equals(prev.getActivePlayer()) && player.getSlot().isOwn()) {
            connection.send(new ToggleClockMessage(gameId, player.getIndex()));
        }

        boolean gameIsOver = isOver();
        if (gameIsOver) {
            if (state.getTurnPlayer().getSlot().isOwn()) { // send messages only from one client
                connection.send(new ToggleClockMessage(gameId, null));
                connection.send(new GameOverMessage(gameId));
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
                    if (action.getOptions() != null) { // bazaar actions can be empty, handled in differente way
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
        undoState = undoState.prepend(state);
    }

    private void clearUndo() {
        undoState = List.empty();
    }

    public boolean isUndoAllowed() {
        return !undoState.isEmpty();
    }

    public void undo() {
        if (undoState.isEmpty()) {
            throw new IllegalStateException();
        }
        Tuple2<GameState, List<GameState>> head = undoState.pop2();
        undoState = head._2;
        replaceState(head._1);
    }

    @WsSubscribe
    public void handleSlotMessage(SlotMessage msg) {
        slotSupportedExpansions[msg.getNumber()] = msg.getSupportedExpansions();
        post(new SupportedExpansionsChangeEvent(mergeSupportedExpansions()));
    }

    @WsSubscribe
    public void handleInGameMessage(WsReplayableMessage msg) {
        markUndo();
        replay = replay.prepend(msg);
        if (msg instanceof WsSeedMeesage) {
            updateRandomSeed(((WsSeedMeesage)msg).getSeed());
        }
        GameState newState = phaseReducer.apply(state, msg);
        Player activePlayer = newState.getActivePlayer();
        if (activePlayer == null || !activePlayer.equals(undoState.get().getActivePlayer())) {
            clearUndo();
        }
        replaceState(newState);
    }

    @WsSubscribe
    public void handleClockMessage(ClockMessage msg) {
        int idx = clocks.indexWhere(c -> c.isRunning());
        if (idx != -1) {
            clocks = clocks.update(idx, new PlayerClock(msg.getClocks()[idx], false));
        }
        if (msg.getRunning() != null) {
            idx = msg.getRunning();
            clocks = clocks.update(idx, new PlayerClock(msg.getClocks()[idx], true));
        }

        post(new ClockUpdateEvent(clocks, msg.getRunning()));
    }


    private EnumSet<Expansion> mergeSupportedExpansions() {
        EnumSet<Expansion> merged = null;
        for (int i = 0; i < slotSupportedExpansions.length; i++) {
            Expansion[] supported = slotSupportedExpansions[i];
            if (supported == null) continue;
            if (merged == null) {
                merged = EnumSet.allOf(Expansion.class);
            }
            EnumSet<Expansion> supp = EnumSet.noneOf(Expansion.class);
            Collections.addAll(supp, supported);
            merged.retainAll(supp);
        }
        return merged;
    }

    @Override
    public void post(Event event) {
        eventBus.post(event);
    }

    //TODO decouple from GameController ?
    public void start(GameController gc, List<WsReplayableMessage> replay) {
        this.replay = replay;
        phaseReducer = new GameStatePhaseReducer(setup, gc);
        GameStateBuilder builder = new GameStateBuilder(setup, slots, gc.getConfig());

        // 1. create state with basic config
        GameState state = builder.createInitialState();
        this.state = state; // set state to get proper state diff against empty state later (in replacedState)
        clocks = state.getPlayers().getPlayers().map(p -> new PlayerClock(0));

        // 2. notify started game - event handlers requires initial state with game config to be set
        post(new GameStartedEvent());

        // 3. trigger initial board changes - make it after started event to propagate all event correctly to GameView
        Phase firstPhase = phaseReducer.getFirstPhase();
        state = builder.createReadyState(state);
        state = state.setPhase(firstPhase.getClass());
        state = phaseReducer.applyStepResult(firstPhase.enter(state));
        for (WsReplayableMessage msg : replay) {
            if (msg instanceof WsSeedMeesage) {
                updateRandomSeed(((WsSeedMeesage) msg).getSeed());
            }
            state = phaseReducer.apply(state, msg);
        }
        replaceState(state);
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

    public Random getRandom() {
        return random;
    }

    public void updateRandomSeed(long seed) {
        random.setSeed(seed);
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
}
