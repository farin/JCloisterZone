package com.jcloisterzone.game;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Meeple;


public abstract class Capability extends GameEventAdapter {

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
    protected Tile getTile() {
        return game.getCurrentTile();
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

    public void prepareActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
    }

    public void prepareFollowerActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
    }

    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        return true;
    }

    public void scoreCompleted(CompletableScoreContext ctx) {
    }

    public void turnCleanUp() {
    }

    public void finalScoring() {
    }

    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replace("Capability", "");
    }

}
