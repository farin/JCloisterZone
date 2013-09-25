package com.jcloisterzone.game.phase;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BridgeCapability;

public class TilePhase extends Phase {

    public TilePhase(Game game) {
        super(game);
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

        boolean bridgeRequired = false;
        if (game.hasCapability(Capability.BRIDGE)) {
            bridgeRequired = !getBoard().isPlacementAllowed(tile, p);
        }

        getBoard().add(tile, p);
        if (tile.getTower() != null) {
            game.getTowerCapability().registerTower(p);
        }
        game.fireGameEvent().tilePlaced(tile);

        if (bridgeRequired) {
            BridgeCapability bcb = game.getBridgeCapability();
            Sites sites = bcb.prepareMandatoryBridgeAction().getSites();

            assert sites.size() == 1;
            Position pos = sites.keySet().iterator().next();
            Location loc = sites.get(pos).iterator().next();

            bcb.decreaseBridges(getActivePlayer());
            bcb.deployBridge(pos, loc);
        }
        getBoard().mergeFeatures(tile);

        next();
    }
}
