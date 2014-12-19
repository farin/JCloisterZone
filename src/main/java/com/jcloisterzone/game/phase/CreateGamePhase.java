package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ClassToInstanceMap;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePackFactory;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.PreparedGameConfigLoader;
import com.jcloisterzone.config.PreparedGameConfig;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.SlotMessage;


public class CreateGamePhase extends ServerAwarePhase {

    private final class PlayerSlotComparator implements Comparator<PlayerSlot> {
        @Override
        public int compare(PlayerSlot o1, PlayerSlot o2) {
            if (o1.getSerial() == null) {
                return o2.getSerial() == null ? 0 : 1;
            }
            if (o2.getSerial() == null) return -1;
            if (o1.getSerial() < o2.getSerial()) return -1;
            if (o1.getSerial() > o2.getSerial()) return 1;
            return 0;
        }
    }

    protected PlayerSlot[] slots;
    protected Expansion[][] slotSupportedExpansions = new Expansion[PlayerSlot.COUNT][];

    public CreateGamePhase(Game game, Connection conn) {
        super(game, conn);
    }

    public void setSlots(PlayerSlot[] slots) {
        this.slots = slots;
    }

    public PlayerSlot[] getPlayerSlots() {
        return slots;
    }


    @WsSubscribe
    public void handleSlotMessage(SlotMessage msg) {
        slotSupportedExpansions[msg.getNumber()] = msg.getSupportedExpansions();
        game.post(new SupportedExpansionsChangeEvent(mergeSupportedExpansions()));
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


    private Phase addPhase(Phase next, Phase phase) {
        if (!phase.isActive()) return next;

        ClassToInstanceMap<Phase> phases = game.getPhases();
        phases.put(phase.getClass(), phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    protected void preparePhases() {
        Phase last, next = null;
        //if there isn't assignment - phase is out of standard flow
               addPhase(next, new GameOverPhase(game));
        next = last = addPhase(next, new CleanUpTurnPhase(game));
        next = addPhase(next, new BazaarPhase(game, getConnection()));
        next = addPhase(next, new CleanUpTurnPartPhase(game));
        next = addPhase(next, new CornCirclePhase(game));
        next = addPhase(next, new EscapePhase(game));

        if (game.hasRule(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
            addPhase(next, new DragonMovePhase(game));
            next = addPhase(next, new DragonPhase(game));
        }

        next = addPhase(next, new WagonPhase(game));
        next = addPhase(next, new ScorePhase(game));
        next = addPhase(next, new CastlePhase(game));

        if (!game.hasRule(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
               addPhase(next, new DragonMovePhase(game));
               next = addPhase(next, new DragonPhase(game));
        }

        next = addPhase(next, new PhantomPhase(game));
               addPhase(next, new TowerCapturePhase(game));
               addPhase(next, new FlierActionPhase(game));
        next = addPhase(next, new ActionPhase(game));
        next = addPhase(next, new PlaguePhase(game));
        next = addPhase(next, new TilePhase(game));
        next = addPhase(next, determineDrawPhase(game));
        next = addPhase(next, new AbbeyPhase(game));
        next = addPhase(next, new FairyPhase(game));
        setDefaultNext(next); //set next phase for this (CreateGamePhase) instance
        last.setDefaultNext(next); //after last phase, the first is default
    }

    private Phase determineDrawPhase(Game game) {
		if(game.isPreparedGame())
		{
			return new PreparedDrawPhase(game, getConnection());
		}
		else
		{
			return new DrawPhase(game, getConnection());
		}
	}

	private void createPlayers() {
        List<Player> players = new ArrayList<>();
        Arrays.sort(slots, new PlayerSlotComparator());
        for (int i = 0; i < slots.length; i++) {
            PlayerSlot slot = slots[i];
            if (slot.isOccupied()) {
                Player player = new Player(slot.getNickname(), i, slot);
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }
        game.setPlayers(players, 0);
    }

    protected Snapshot getSnapshot() {
        return null;
    }

    protected void initializePlayersMeeples() {
        for (Player player : game.getAllPlayers()) {
            for (int i = 0; i < SmallFollower.QUANTITY; i++) {
                player.addMeeple(new SmallFollower(game, player));
            }
            game.initPlayer(player);
        }
    }

    protected void preparePlayers() {
        createPlayers();
        initializePlayersMeeples();
    }

    protected void prepareTilePack() {
        TilePackFactory tilePackFactory = new TilePackFactory();
        tilePackFactory.setGame(game);
        tilePackFactory.setConfig(game.getConfig());
        tilePackFactory.setExpansions(game.getExpansions());
        game.setTilePack(tilePackFactory.createTilePack());
        getTilePack().setGroupState("default", TileGroupState.ACTIVE);
        getTilePack().setGroupState("count", TileGroupState.ACTIVE);
        getTilePack().setGroupState("wind-rose-initial", TileGroupState.ACTIVE);
        game.begin();
    }

    protected void preplaceTiles() {
        for (Tile preplaced : ((DefaultTilePack)getTilePack()).drawPrePlacedActiveTiles()) {
            game.getBoard().add(preplaced, preplaced.getPosition(), true);
            game.getBoard().mergeFeatures(preplaced);
            game.post(new TileEvent(TileEvent.PLACEMENT, null, preplaced, preplaced.getPosition()));
        }
    }

    protected void prepareAiPlayers() {
        for (PlayerSlot slot : slots) {
            if (slot != null && slot.isAi() && slot.isOwn()) {
                try {
                    AiPlayer ai = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                    ai.setGame(game);
                    ai.setServer(getConnection(), getServer());
                    for (Player player : game.getAllPlayers()) {
                        if (player.getSlot().getNumber() == slot.getNumber()) {
                            ai.setPlayer(player);
                            break;
                        }
                    }
                    game.getEventBus().register(ai);
                    logger.info("AI player created - " + slot.getAiClassName());
                } catch (Exception e) {
                    logger.error("Unable to create AI player", e);
                }
            }
        }
    }

    protected void prepareCapabilities() {
        for (Expansion exp : game.getExpansions()) {
            game.getCapabilityClasses().addAll(Arrays.asList(exp.getCapabilities()));
        }

        DebugConfig debugConfig = game.getConfig().getDebug();
        if (debugConfig != null && debugConfig.getOff_capabilities() != null) {
            List<String> offNames =  debugConfig.getOff_capabilities();
            Set<Class<? extends Capability>> off = new HashSet<>();
            for (String tok : offNames) {
                tok = tok.trim();
                try {
                    String className = "com.jcloisterzone.game.capability."+tok+"Capability";
                    @SuppressWarnings("unchecked")
                    Class<? extends Capability> clazz = (Class<? extends Capability>) Class.forName(className);
                    off.add(clazz);
                } catch (Exception e) {
                    logger.warn("Invalid capability name: " + tok, e);
                }
            }
            game.getCapabilityClasses().removeAll(off);
        }
    }

	protected void preparePreparedGame()
	{
        //load prepared tiles
        PreparedGameConfigLoader pcl = new PreparedGameConfigLoader();
        PreparedGameConfig preparedTiles = pcl.load();
        game.setPreparedTiles(preparedTiles);
        if(preparedTiles.getDraw() != null)	
        {
        	game.setPreparedGame(true);
        }
        else
        {
        	game.setPreparedGame(false);
        }
		
	}
    public void startGame() {
        //temporary code should be configured by player as rules
        prepareCapabilities();

        game.start();
        preparePreparedGame();
        preparePlayers();
        preparePhases();
        prepareTilePack();
        prepareAiPlayers();

        game.post(new GameStateChangeEvent(GameStateChangeEvent.GAME_START, getSnapshot()));
        preplaceTiles();
        game.post(new PlayerTurnEvent(game.getTurnPlayer()));
        //game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());

        next();
    }

}