package com.jcloisterzone.game.phase;

import java.util.Map.Entry;
import java.util.Set;

import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.TileEvent;
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
        TilePlacementAction action = new TilePlacementAction(game.getCurrentTile());
        for (Entry<Position, Set<Rotation>> entry: getBoard().getAvailablePlacements().entrySet()) {
            for (Rotation rotation : entry.getValue()) {
                action.add(new TilePlacement(entry.getKey(), rotation));
            }
        }
        action.setOccupiedPositions(getBoard().getOccupied());
        game.post(new SelectActionEvent(getActivePlayer(), action, false));
    }

    @Override
    public void loadGame(Snapshot snapshot) {
         String tileId = snapshot.getNextTile();
         Tile tile = game.getTilePack().drawTile(tileId);
         game.setCurrentTile(tile);
         game.getBoard().refreshAvailablePlacements(tile);
         game.post(new TileEvent(TileEvent.DRAW, getActivePlayer(), tile, null));
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
        game.post(new TileEvent(TileEvent.PLACEMENT, getActivePlayer(), tile, p));

        if (bridgeRequired) {
            BridgeAction action = bridgeCap.prepareMandatoryBridgeAction();

            assert action.getOptions().size() == 1;
            FeaturePointer bp = action.getOptions().iterator().next();

            bridgeCap.decreaseBridges(getActivePlayer());
            bridgeCap.deployBridge(bp.getPosition(), bp.getLocation(), true);
        }
        getBoard().mergeFeatures(tile);

        next();
    }
}
