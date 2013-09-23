package com.jcloisterzone.game;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;


public class ExtensionsDelegate implements GameDelegation {

    private final Game game;

    public ExtensionsDelegate(Game game) {
        this.game = game;
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.initTile(tile, xml);
        }
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.initFeature(tile, feature, xml);
        }
    }

    @Override
    public void initPlayer(Player player) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.initPlayer(player);
        }
    }

    @Override
    public String getTileGroup(Tile tile) {
        for (GameDelegation eg: game.getExtensions()) {
            String group = eg.getTileGroup(tile);
            if (group != null) return group;
        }
        return null;
    }

    @Override
    public void begin() {
        for(GameDelegation eg: game.getExtensions()) {
            eg.begin();
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.prepareActions(actions, commonSites);
        }
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, Sites commonSites) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.prepareFollowerActions(actions, commonSites);
        }
    }

    @Override
    public void scoreCompleted(CompletableScoreContext ctx) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.scoreCompleted(ctx);
        }
    }

    @Override
    public void turnCleanUp() {
        for(GameDelegation eg: game.getExtensions()) {
            eg.turnCleanUp();
        }

    }

    @Override
    public void finalScoring() {
        for(GameDelegation eg: game.getExtensions()) {
            eg.finalScoring();
        }
    }

    @Override
    public boolean isSpecialPlacementAllowed(Tile tile, Position p) {
        for(GameDelegation eg: game.getExtensions()) {
            if (eg.isSpecialPlacementAllowed(tile, p)) return true;
        }
        return false;
    }

    @Override
    public boolean isPlacementAllowed(Tile tile, Position p) {
        for(GameDelegation eg: game.getExtensions()) {
            if (! eg.isPlacementAllowed(tile, p)) return false;
        }
        return true;
    }

    public void saveTileToSnapshot(Tile tile, Document doc, Element tileNode) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.saveTileToSnapshot(tile, doc, tileNode);
        }
    }

    @Override
    public void loadTileFromSnapshot(Tile tile, Element tileNode) {
        for(GameDelegation eg: game.getExtensions()) {
            eg.loadTileFromSnapshot(tile, tileNode);
        }
    }
}
