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
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;


public class CapabilityController implements GameDelegation {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Game game;

    public Game getGame() {
        return game;
    }
    public void setGame(Game game) {
        this.game = game;
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

    /**
     * @return object copy or null if expansion is stateless
     */
    public CapabilityController copy() {
        return null;
    }

    public void saveToSnapshot(Document doc, Element node) {
    }

    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
    }

    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
    }

    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
    }

    //--- delegation adapter methods ---

    @Override
    public void initTile(Tile tile, Element xml) {
    }


    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
    }

    @Override
    public String getTileGroup(Tile tile) {
        return null;
    }

    @Override
    public void initPlayer(Player player) {
    }

    @Override
    public void begin() {
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, Sites commonSites) {
    }

    @Override
    public void scoreCompleted(CompletableScoreContext ctx) {
    }

    @Override
    public void turnCleanUp() {

    }

    @Override
    public void finalScoring() {
    }

    @Override
    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        return true;
    }

}
