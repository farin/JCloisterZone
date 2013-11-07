package com.jcloisterzone.game.phase;

import java.util.List;

import org.ini4j.Profile.Section;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.rmi.ServerIF;


public class DrawPhase extends ServerAwarePhase {

    private List<String> debugTiles;
    private final BazaarCapability bazaarCap;

    public DrawPhase(Game game, ServerIF server) {
        super(game, server);
        Section debugSection = game.getConfig().get("debug");
        if (debugSection != null) {
            debugTiles = debugSection.getAll("draw");
        }
        bazaarCap = game.getCapability(BazaarCapability.class);
    }

    private boolean makeDebugDraw() {
        if (debugTiles != null && debugTiles.size() > 0) { //for debug purposes only
            String tileId = debugTiles.remove(0);
            if (tileId.equals("!")) {
                next(GameOverPhase.class);
                return true;
            }
            TilePack tilePack = getTilePack();
            Tile tile = tilePack.drawTile(tileId);
            if (tile == null) {
                logger.warn("Invalid debug draw id: " + tileId);
            } else {
                if (game.hasCapability(RiverCapability.class) && tile.getRiver() == null && (tilePack.isGroupActive("river-start") || tilePack.isGroupActive("river"))) {
                    game.getCapability(RiverCapability.class).activateNonRiverTiles();
                    tilePack.deactivateGroup("river-start");
                    game.setCurrentTile(tile); //recovery from lake placement
                }
                nextTile(tile);
                return true;
            }
        }
        return false;
    }

    @Override
    public void enter() {
        if (getTilePack().isEmpty()) {
            next(GameOverPhase.class);
            return;
        }
        if (bazaarCap != null) {
            Tile tile = bazaarCap.drawNextTile();
            if (tile != null) {
                nextTile(tile);
                return;
            }
        }

        if (makeDebugDraw()) {
            return;
        }
        if (isLocalPlayer(getActivePlayer())) {
            //call only from one client (from the active one)
            getServer().selectTiles(getTilePack().size(), 1);
        }
    }



    @Override
    public void drawTiles(int[] tileIndex) {
        assert tileIndex.length == 1;
        Tile tile = getTilePack().drawTile(tileIndex[0]);
        nextTile(tile);
    }

    private void nextTile(Tile tile) {
        game.setCurrentTile(tile);
        getBoard().refreshAvailablePlacements(tile);
        if (getBoard().getAvailablePlacementPositions().isEmpty()) {
            getBoard().discardTile(tile);
            next(DrawPhase.class);
            return;
        }
        game.fireGameEvent().tileDrawn(tile);
        next();
    }

}
