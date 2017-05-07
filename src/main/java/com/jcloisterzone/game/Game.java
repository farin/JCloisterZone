package com.jcloisterzone.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashCode;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.BridgeEvent;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.GoldChangeEvent;
import com.jcloisterzone.event.Idempotent;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.Undoable;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.ScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.NeutralFigure;
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
public class Game extends GameSettings implements EventProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** pack of remaining tiles */
    private TilePack tilePack;
    /** pack of remaining tiles */
    private Tile currentTile;
    /** game board, contains placed tiles */
    private Board board;

    /** list of players in game */
    private Player[] plist;

    private final List<NeutralFigure> neutralFigures = new ArrayList<>();

    /** player in turn */
    private Player turnPlayer;

    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();
    private Phase phase;

    private List<Capability> capabilities = new ArrayList<>(); //TODO change to map?
    private FairyCapability fairyCapability; //shortcut - TODO remove

    private ArrayList<Undoable> lastUndoable = new ArrayList<>();
    private Phase lastUndoablePhase;

    private final EventBus eventBus = new EventBus(new EventBusExceptionHandler("game event bus"));
    //events are delayed and fired after phase is handled (and eventually switched to the new one) - important especially for AI handlers to not start before swithc is done
    private final Deque<Event> eventQueue = new ArrayDeque<>();

    private int idSequenceCurrVal = 0;

    private final Random random;
    private long randomSeed;

    public Game(String gameId) {
        super(gameId);
        HashCode hash = HashCode.fromBytes(gameId.getBytes());
        this.randomSeed = hash.asLong();
        this.random = new Random(randomSeed);
    }

    public Game(String gameId, long randomSeed) {
        super(gameId);
        this.randomSeed = randomSeed;
        this.random = new Random(randomSeed);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    public Undoable getLastUndoable() {
        return lastUndoable.size() == 0 ? null : lastUndoable.get(lastUndoable.size()-1);
    }

    public void clearLastUndoable() {
        lastUndoable.clear();
    }

    private boolean isUiSupportedUndo(Event event) {
        if (event instanceof TileEvent && event.getType() == TileEvent.PLACEMENT) return true;
        if (event instanceof MeepleEvent && ((MeepleEvent) event).getTo() != null) return true;
        if (event instanceof BridgeEvent && event.getType() == BridgeEvent.DEPLOY) return true;
        if (event instanceof GoldChangeEvent) return true;
        if (event instanceof ScoreEvent && ((ScoreEvent)event).getCategory() == PointCategory.WIND_ROSE) return true;
        return false;
    }

    @Override
    public void post(Event event) {
        eventQueue.add(event);
        if (event instanceof PlayEvent && !event.isUndo()) {
            if (isUiSupportedUndo(event)) {
                if ((event instanceof BridgeEvent && ((BridgeEvent)event).isForced()) ||
                     event instanceof GoldChangeEvent && ((GoldChangeEvent)event).getPos().equals(getCurrentTile().getPosition()) ||
                     event instanceof ScoreEvent) {
                    //just add to chain after tile event
                    lastUndoable.add((Undoable) event);
                } else {
                    lastUndoable.clear();
                    lastUndoable.add((Undoable) event);
                    lastUndoablePhase = phase;
                }
            } else {
                if (event.getClass().getAnnotation(Idempotent.class) == null) {
                    lastUndoable.clear();
                    lastUndoablePhase = null;
                }
            }
        }
        // process capabilities after undo processing
        // capability can trigger another event and order is important! (eg. windrose scoring)
        for (Capability capability: capabilities) {
            capability.handleEvent(event);
        }
    }

    public void flushEventQueue() {
        Event event;
        while ((event = eventQueue.poll()) != null) {
            eventBus.post(event);
        }
    }

    public boolean isUndoAllowed() {
        return lastUndoable.size() > 0;
    }

    public void undo() {
    	if (!isUndoAllowed()) {
    		logger.warn("Undo is not allowed");
    		return;
    	}
        for (int i = lastUndoable.size()-1; i >= 0; i--) {
            Undoable ev = lastUndoable.get(i);
            Event inverse = ev.getInverseEvent();
            inverse.setUndo(true);

            ev.undo(this);
            post(inverse); //should be post inside undo? silent vs. firing undo?
        }
        phase = lastUndoablePhase;
        lastUndoable.clear();
        lastUndoablePhase = null;
        phase.reenter();
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public void setCurrentTile(Tile currentTile) {
        this.currentTile = currentTile;
    }

    public PlayerSlot[] getPlayerSlots() {
        // need to match subtypes, can't use getInstance on phases
        for (Phase phase : phases.values()) {
            if (phase instanceof CreateGamePhase) {
                return ((CreateGamePhase)phase).getPlayerSlots();
            }
        }
        return null;
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

    public List<NeutralFigure> getNeutralFigures() {
        return neutralFigures;
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

    public Player getPrevPlayer(Player p) {
        int playerIndex = p.getIndex();
        int prevPlayerIndex = playerIndex == 0 ? plist.length - 1 : playerIndex - 1;
        return getPlayer(prevPlayerIndex);
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

    public Random getRandom() {
        return random;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void updateRandomSeed(long update) {
        randomSeed = randomSeed ^ update;
        random.setSeed(randomSeed);
    }

    public Meeple getMeeple(MeeplePointer mp) {
        for (Meeple m : getDeployedMeeples()) {
            if (m.at(mp)) return m;
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
            if (!excludeFinished && hasCapability(PrincessCapability.class) && getBooleanValue(CustomRule.PRINCESS_MUST_REMOVE_KNIGHT)) {
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
        boolean isFairyScore = false;
        if (fairyCapability != null) {
            for (Follower f : ctx.getFollowers()) {
                if (f.getPlayer() == p && fairyCapability.isNextTo(f)) {
                    isFairyScore = true;
                    break;
                }
            }
        }
        if (isFairyScore) {
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
        if (fairyCapability != null) {
            Set<Player> fairyPlayersWithoutMayority = new HashSet<>();
            for (Follower f : ctx.getFollowers()) {
                Player owner = f.getPlayer();
                if (fairyCapability.isNextTo(f) && !players.contains(owner)
                    && !fairyPlayersWithoutMayority.contains(owner)) {
                    fairyPlayersWithoutMayority.add(owner);

                    owner.addPoints(FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY);
                    post(new ScoreEvent(f.getFeature(), FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY, f));
                }
            }
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
        for (Capability cap: capabilities) {
            cap.postPrepareActions(actions);
        }

        //to simplify capability iterations, allow returning empty actions (eg tower can add empty meeple action when no open tower exists etc)
        //and then filter them out at end
        Iterator<PlayerAction<?>> iter = actions.iterator();
        while (iter.hasNext()) {
            PlayerAction<?> action = iter.next();
            if (action.isEmpty()) {
                iter.remove();
            }
        }
    }


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

    public void finalScoring(ScoringStrategy strategy) {
        for (Capability cap: capabilities) {
            cap.finalScoring(strategy);
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
}
