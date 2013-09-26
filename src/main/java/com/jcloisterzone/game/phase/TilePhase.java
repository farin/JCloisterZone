package com.jcloisterzone.game.phase;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.TowerCapability;

public class TilePhase extends Phase {

    private final BridgeCapability bridgeCap;

    public TilePhase(Game game) {
        super(game);
        bridgeCap = game.getCapability(BridgeCapability.class);
    }

    @Override
    public void enter() {
        Map<Position, Set<Rotation>> freezed = Maps.newHashMap(getBoard().getAvailablePlacements());
        notifyUI(new TilePlacementAction(game.getCurrentTile(), freezed), false);
    }

    @Override
    public void loadGame(Snapshot snapshot) {
         String tileId = snapshot.getNextTile();
         Tile tile = game.getTilePack().drawTile(tileId);
         game.setCurrentTile(tile);
         game.getBoard().refreshAvailablePlacements(tile);
         game.fireGameEvent().tileDrawn(tile);
    }

    @Override
    public void placeTile(Rotation rotation, Position p) {
        Tile tile = getTile();
        tile.setRotation(rotation);

        boolean bridgeRequired = bridgeCap != null && !getBoard().isPlacementAllowed(tile, p);

        getBoard().add(tile, p);
        if (tile.getTower() != null) {
            game.getCapability(TowerCapability.class).registerTower(p);
        }
        game.fireGameEvent().tilePlaced(tile);

        if (bridgeRequired) {
            Sites sites = bridgeCap.prepareMandatoryBridgeAction().getSites();

            assert sites.size() == 1;
            Position pos = sites.keySet().iterator().next();
            Location loc = sites.get(pos).iterator().next();

            bridgeCap.decreaseBridges(getActivePlayer());
            bridgeCap.deployBridge(pos, loc);
        }
        getBoard().mergeFeatures(tile);

        next();
    }
}
