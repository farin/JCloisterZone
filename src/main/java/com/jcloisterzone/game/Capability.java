package com.jcloisterzone.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;


public abstract class Capability {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;

    public Capability(Game game) {
        this.game = game;
    }

    public Object backup() {
        return null;
    }
    public void restore(Object data) {
        //unpack data created by backup and fill itself
    }

    protected TilePack getTilePack() {
        return game.getTilePack();
    }
    protected Board getBoard() {
        return game.getBoard();
    }
    protected Tile getCurrentTile() {
        return game.getCurrentTile();
    }

    /* no @Subscribe for Capabilities
     * it cause post from another event handler and makes trouble with AI tasks
     * */
    public void handleEvent(Event event) {
    }

    public void saveToSnapshot(Document doc, Element node) {
    }

    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
    }

    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
    }

    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
    }

    public void initTile(Tile tile, Element xml) {
    }

    public void initFeature(Tile tile, Feature feature, Element xml) {
    }

    public String getTileGroup(Tile tile) {
        return null;
    }

    public void initPlayer(Player player) {
    }

    public void begin() {
    }

    /** convenient method to find follower action in all actions */
    protected List<MeepleAction> findFollowerActions(List<PlayerAction<?>> actions) {
        List<MeepleAction> followerActions = new ArrayList<>();
        for (PlayerAction<?> a : actions) {
            if (a instanceof MeepleAction) {
                MeepleAction ma = (MeepleAction) a;
                if (Follower.class.isAssignableFrom(ma.getMeepleType())) {
                    followerActions.add(ma);
                }
            }
        }
        return followerActions;
    }

    /** convenient method to find follower action in all actions, or create new if player has follower and action doesn't exists*/
    protected List<MeepleAction> findAndFillFollowerActions(List<PlayerAction<?>> actions) {
        List<MeepleAction> followerActions = findFollowerActions(actions);
        Set<Class<? extends Meeple>> hasAction = new HashSet<>();
        for (MeepleAction ma : followerActions) {
            hasAction.add(ma.getMeepleType());
        }

        for (Follower f : game.getActivePlayer().getFollowers()) {
            if (f.isInSupply() && !hasAction.contains(f.getClass())) {
                MeepleAction ma = new MeepleAction(f.getClass());
                actions.add(ma);
                followerActions.add(ma);
                hasAction.add(f.getClass());
            }
        }
        return followerActions;
    }

    public void extendFollowOptions(Set<FeaturePointer> followerOptions) {
    }

    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
    }

    public void postPrepareActions(List<PlayerAction<?>> actions) {
    }

    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        return true;
    }

    public void scoreCompleted(CompletableScoreContext ctx) {
    }

    public void turnCleanUp() {
    }

    public void turnPartCleanUp() {
    }


    public void finalScoring(ScoringStrategy strategy) {
    }

    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replace("Capability", "");
    }

}
