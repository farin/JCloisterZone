package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.rmi.ServerIF;


public class DrawPhase extends ServerAwarePhase {

    private static final String DEBUG_END_OF_PACK = ".";

    private List<String> debugTiles;
    private final BazaarCapability bazaarCap;
    private final AbbeyCapability abbeyCap;

    public DrawPhase(Game game, ServerIF server) {
        super(game, server);
        DebugConfig debugConfig = game.getConfig().getDebug();
        if (debugConfig != null) {
            debugTiles = debugConfig.getDraw();
        }
        bazaarCap = game.getCapability(BazaarCapability.class);
        abbeyCap = game.getCapability(AbbeyCapability.class);
    }

    private boolean makeDebugDraw() {
        if (debugTiles != null && debugTiles.size() > 0) { //for debug purposes only
            String tileId = debugTiles.remove(0);
            if (tileId.equals(DEBUG_END_OF_PACK)) {
                next(GameOverPhase.class);
                return true;
            }
            TilePack tilePack = getTilePack();
            Tile tile = tilePack.drawTile(tileId);
            if (tile == null) {
                logger.warn("Invalid debug draw id: " + tileId);
            } else {
                boolean riverActive = tilePack.getGroupState("river-start") == TileGroupState.ACTIVE || tilePack.getGroupState("river") == TileGroupState.ACTIVE;
                if (game.hasCapability(RiverCapability.class) && tile.getRiver() == null && riverActive) {
                    game.getCapability(RiverCapability.class).activateNonRiverTiles();
                    tilePack.setGroupState("river-start", TileGroupState.RETIRED);
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
            if (abbeyCap != null && !getActivePlayer().equals(abbeyCap.getAbbeyRoundLastPlayer())) {
                if (abbeyCap.getAbbeyRoundLastPlayer() == null) {
                    abbeyCap.setAbbeyRoundLastPlayer(getActivePlayer());
                }
                next(CleanUpTurnPartPhase.class);
                return;
            }
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
