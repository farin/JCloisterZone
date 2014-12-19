package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.GetRandSampleMessage;
import com.jcloisterzone.wsio.message.RandSampleMessage;


public class DrawPhase extends ServerAwarePhase {

    private static final String DEBUG_END_OF_PACK = ".";

    private List<String> debugTiles;
    private final BazaarCapability bazaarCap;
    private final AbbeyCapability abbeyCap;

    public DrawPhase(Game game, Connection conn) {
        super(game, conn);
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
        if(checkForEarlyPhaseChange())
        {
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
            getConnection().send(new GetRandSampleMessage(game.getGameId(), "draw", getTilePack().size(), 1));
        }
    }

	public boolean checkForEarlyPhaseChange() {
		if (getTilePack().isEmpty()) {
            if (abbeyCap != null && !getActivePlayer().equals(abbeyCap.getAbbeyRoundLastPlayer())) {
                if (abbeyCap.getAbbeyRoundLastPlayer() == null) {
                    abbeyCap.setAbbeyRoundLastPlayer(getActivePlayer());
                }
                next(CleanUpTurnPartPhase.class);
                return true;
            }
            next(GameOverPhase.class);
            return true;
        }
		return false;
	}

    @WsSubscribe
    public void handleRandSample(RandSampleMessage msg) {
        if (!msg.getName().equals("draw") || msg.getPopulation() != getTilePack().size()) {
            logger.error("Invalid message");
            return;
        }
        Tile tile = drawTileFromPack(msg);
        if (tile == null)
        {
        	next(GameOverPhase.class);
        }
        nextTile(tile);
    }

	public Tile drawTileFromPack(RandSampleMessage msg) {
		Tile tile = getTilePack().drawTile(msg.getValues()[0]);
		return tile;
	}


    private void nextTile(Tile tile) {
        game.setCurrentTile(tile);
        getBoard().refreshAvailablePlacements(tile);
        if (getBoard().getAvailablePlacementPositions().isEmpty()) {
            getBoard().discardTile(tile);
            next(DrawPhase.class);
            return;
        }
        game.post(new TileEvent(TileEvent.DRAW, getActivePlayer(), tile, null));
        next();
    }

}
