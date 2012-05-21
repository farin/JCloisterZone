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
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;

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
    public void placeTile(Rotation rotation, Position p) {
        Tile tile = getTile();
        tile.setRotation(rotation);

        boolean bridgeRequired = false;
        if (game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) {
            bridgeRequired = ! getBoard().isPlacementAllowed(tile, p);
        }

        getBoard().add(tile, p);
        if (tile.getTower() != null) {
            game.getTowerGame().registerTower(p);
        }
        game.fireGameEvent().tilePlaced(tile);

        if (bridgeRequired) {
            BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
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
