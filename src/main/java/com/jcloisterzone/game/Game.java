package com.jcloisterzone.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.PreparedGameConfig;
import com.jcloisterzone.config.PreparedGameConfigLoader;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.Idempotent;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.Undoable;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.ScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;


/**
 * Other information than board needs in game. Contains players with their
 * points, followers ... and game rules of current game.
 */
public class Game extends GameSettings {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    private Config config;
    
    private PreparedGameConfig preparedTiles;
    
    private boolean isPreparedGame;

    /** pack of remaining tiles */
    private TilePack tilePack;
    /** pack of remaining tiles */
    private Tile currentTile;
    /** game board, contains placed tiles */
    private Board board;

    /** list of players in game */
    private Player[] plist;
    /** rules of current game */

    /** player in turn */
    private Player turnPlayer;

    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();
    private Phase phase;

    private List<Capability> capabilities = new ArrayList<>();
    private FairyCapability fairyCapability; //shortcut

    private Undoable lastUndoable;
    private Phase lastUndoablePhase;

    private final EventBus eventBus = new EventBus(new EventBusExceptionHandler("game event bus"));
    //events are delayed and fired after phase is handled (and eventually switched to the new one) - important especially for AI handlers to not start before swithc is done
    private final Deque<Event> eventQueue = new ArrayDeque<>();

    private int idSequenceCurrVal = 0;


    public Game(String gameId) {
        super(gameId);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void post(Event event) {
        eventQueue.add(event);
        for (Capability capability: capabilities) {
            capability.handleEvent(event);
        }
        if (event instanceof PlayEvent) {
            if (event instanceof TileEvent && event.getType() == TileEvent.PLACEMENT) {
                lastUndoable = (Undoable) event;
                lastUndoablePhase = phase;
            } else {
                if (event.getClass().getAnnotation(Idempotent.class) == null) {
                    lastUndoable = null;
                    lastUndoablePhase = null;
                }
            }
        }
    }

    public void flushEventQueue() {
        Event event;
        while ((event = eventQueue.poll()) != null) {
            eventBus.post(event);
        }
    }

    public boolean isUndoAllowed() {
        return lastUndoable != null;
    }

    public void undo() {
        //proof of concept
        if (lastUndoable instanceof TileEvent) {
            Tile tile = ((TileEvent)lastUndoable).getTile();
            Position pos = tile.getPosition();

            lastUndoable.undo(this);
            phase = lastUndoablePhase;
            lastUndoable = null;
            lastUndoablePhase = null;

            //post should be in event undo. silent vs firing undo ?
            post(new TileEvent(TileEvent.REMOVE, getActivePlayer(), tile, pos));
            phase.enter();
        }
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public void setCurrentTile(Tile currentTile) {
        this.currentTile = currentTile;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        phase.setEntered(false);
        this.phase = phase;
    }

    public ClassToInstanceMap<Phase> getPhases() {
        return phases;
    }

    public Iterable<Meeple> getDeployedMeeples() {
        Iterable<Meeple> iter = Collections.emptyList();
        for (Player player : plist) {
            iter = Iterables.concat(iter, player.getFollowers(), player.getSpecialMeeples());
        }
        return Iterables.filter(iter, MeeplePredicates.deployed());
    }

    public Player getTurnPlayer() {
        return turnPlayer;
    }

    public void setTurnPlayer(Player turnPlayer) {
        this.turnPlayer = turnPlayer;
        post(new PlayerTurnEvent(turnPlayer));
    }

    /**
     * Returns player who is allowed to make next action.
     * @return
     */
    public Player getActivePlayer() {
        Phase phase = getPhase();
        return phase == null ? null : phase.getActivePlayer();
    }


    /**
     * Ends turn of current active player and make active the next.
     */
    public Player getNextPlayer() {
        return getNextPlayer(turnPlayer);
    }

    public Player getNextPlayer(Player p) {
        int playerIndex = p.getIndex();
        int nextPlayerIndex = playerIndex == (plist.length - 1) ? 0 : playerIndex + 1;
        return getPlayer(nextPlayerIndex);
    }


    /**
     * Return player with the given index.
     * @param i player index
     * @return demand player
     */
    public Player getPlayer(int i) {
        return plist[i];
    }

    /**
     * Returns whole player list
     * @return player list
     */
    public Player[] getAllPlayers() {
        return plist;
    }

    public TilePack getTilePack() {
        return tilePack;
    }

    public void setTilePack(TilePack tilePack) {
        this.tilePack = tilePack;
    }

    public Board getBoard() {
        return board;
    }


    public Meeple getMeeple(final Position p, final Location loc, Class<? extends Meeple> meepleType, Player owner) {
        for (Meeple m : getDeployedMeeples()) {
            if (m.at(p) && m.getLocation().equals(loc)) {
                if (m.getClass().equals(meepleType) && m.getPlayer().equals(owner)) {
                    return m;
                }
            }
        }
        return null;
    }

    public void setPlayers(List<Player> players, int turnPlayer) {
        Player[] plist = players.toArray(new Player[players.size()]);
        this.plist = plist;
        this.turnPlayer = getPlayer(turnPlayer);
    }

    private void createCapabilityInstance(Class<? extends Capability> clazz) {
        if (clazz == null) return;
        try {
            Capability capability = clazz.getConstructor(Game.class).newInstance(this);
            capabilities.add(capability);
        } catch (Exception e) {
            logger.error(e.getMessage(), e); //should never happen
        }
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    @SuppressWarnings("unchecked")
    public <T extends Capability> T getCapability(Class<T> clazz) {
        for (Capability c : capabilities) {
            if (c.getClass().equals(clazz)) return (T) c;
        }
        return null;
    }

    public void start() {
        for (Class<? extends Capability> capability: getCapabilityClasses()) {
            createCapabilityInstance(capability);
        }
        board = new Board(this);
    }

    public boolean isStarted() {
        return !(phase instanceof CreateGamePhase);
    }

    public boolean isOver() {
        return phase instanceof GameOverPhase;
    }


    public Set<FeaturePointer> prepareFollowerLocations() {
        Set<FeaturePointer> followerOptions = prepareFollowerLocations(currentTile, false);
        for (Capability cap: capabilities) {
            cap.extendFollowOptions(followerOptions);
        }
        return followerOptions;
    }

    public Set<FeaturePointer> prepareFollowerLocations(Tile tile, boolean excludeFinished) {
        if (!isDeployAllowed(tile, Follower.class)) return Collections.emptySet();
        Set<FeaturePointer> pointers = new HashSet<>();
        for (Location loc: tile.getUnoccupiedScoreables(excludeFinished)) {
            //exclude finished == false -> just placed tile - it means do not check princess for magic portal
            //TODO very cryptic, refactor
            if (!excludeFinished && hasCapability(PrincessCapability.class) && hasRule(CustomRule.PRINCESS_MUST_REMOVE_KNIGHT)) {
                City princessCity = tile.getCityWithPrincess();
                if (princessCity != null) {
                    continue;
                }
            }
            pointers.add(new FeaturePointer(tile.getPosition(), loc));
        }
        return pointers;
    }

    //scoring helpers

    public void scoreFeature(int points, ScoreContext ctx, Player p) {
        PointCategory pointCategory = ctx.getMasterFeature().getPointCategory();
        p.addPoints(points, pointCategory);
        Follower follower = ctx.getSampleFollower(p);
        boolean isFinalScoring = getPhase() instanceof GameOverPhase;
        ScoreEvent scoreEvent;
        if (fairyCapability != null && follower.at(fairyCapability.getFairyPosition())) {
            p.addPoints(FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY);
            scoreEvent = new ScoreEvent(follower.getFeature(), points+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, pointCategory, follower);
            scoreEvent.setLabel(points+" + "+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT);
        } else {
            scoreEvent = new ScoreEvent(follower.getFeature(), points, pointCategory, follower);
        }
        scoreEvent.setFinal(isFinalScoring);
        post(scoreEvent);
    }

    public void scoreCompletableFeature(CompletableScoreContext ctx) {
        Set<Player> players = ctx.getMajorOwners();
        if (players.isEmpty()) return;
        int points = ctx.getPoints();
        for (Player p : players) {
            scoreFeature(points, ctx, p);
        }
    }

    public int idSequnceNextVal() {
        return ++idSequenceCurrVal;
    }

    // delegation to capabilities

    public void initTile(Tile tile, Element xml) {
        for (Capability cap: capabilities) {
            cap.initTile(tile, xml);
        }
    }

    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Farm && tile.getId().startsWith("CO.")) {
            //this is not part of Count capability because it is integral behaviour valid also when capability is off
            ((Farm) feature).setAdjoiningCityOfCarcassonne(true);
        }
        for (Capability cap: capabilities) {
            cap.initFeature(tile, feature, xml);
        }
    }

    public void initPlayer(Player player) {
        for (Capability cap: capabilities) {
            cap.initPlayer(player);
        }
    }

    public String getTileGroup(Tile tile) {
        for (Capability cap: capabilities) {
            String group = cap.getTileGroup(tile);
            if (group != null) return group;
        }
        return null;
    }

    public void begin() {
        fairyCapability = getCapability(FairyCapability.class);
        for (Capability cap: capabilities) {
            cap.begin();
        }
    }

    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        for (Capability cap: capabilities) {
            cap.prepareActions(actions, followerOptions);
        }
        for (Capability cap: capabilities) { //TODO hack for flier
            cap.postPrepareActions(actions, followerOptions);
        }
    }

//    public void prepareAnyTimeActions(List<PlayerAction> actions) {
//        for (Capability cap: capabilities) {
//            cap.prepareAnyTimeActions(actions);
//        }
//    }

    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        for (Capability cap: capabilities) {
            if (!cap.isDeployAllowed(tile, meepleType)) return false;
        }
        return true;
    }

    public void scoreCompleted(CompletableScoreContext ctx) {
        for (Capability cap: capabilities) {
            cap.scoreCompleted(ctx);
        }
    }

    public void turnPartCleanUp() {
        for (Capability cap: capabilities) {
            cap.turnPartCleanUp();
        }
    }

    public void turnCleanUp() {
        for (Capability cap: capabilities) {
            cap.turnCleanUp();
        }
    }

    public void finalScoring() {
        for (Capability cap: capabilities) {
            cap.finalScoring();
        }
    }

    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        for (Capability cap: capabilities) {
            if (!cap.isTilePlacementAllowed(tile, p)) return false;
        }
        return true;
    }

    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
        for (Capability cap: capabilities) {
            cap.saveTileToSnapshot(tile, doc, tileNode);
        }
    }

    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
        for (Capability cap: capabilities) {
            cap.loadTileFromSnapshot(tile, tileNode);
        }
    }

    @Override
    public String toString() {
        return "Game in " + phase.getClass().getSimpleName() + " phase.";
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

	public PreparedGameConfig getPreparedTiles() {
		return preparedTiles;
	}

	public void setPreparedTiles(PreparedGameConfig preparedTiles) {
		this.preparedTiles = preparedTiles;
	}

	public boolean isPreparedGame() {
		return isPreparedGame;
	}

	public void setPreparedGame(boolean isPreparedGame) {
		this.isPreparedGame = isPreparedGame;
	}
}
