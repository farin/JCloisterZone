package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.BridgeCapability.BridgeToken;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.collection.LinkedHashMap;

public class PlaceBridge implements Reducer {

    private final FeaturePointer ptr;
    private boolean silent;

    public PlaceBridge(FeaturePointer ptr, boolean silent) {
        this.ptr = ptr;
        this.silent = silent;
    }

    public PlaceBridge(FeaturePointer ptr) {
        this(ptr, false);
    }

    @Override
    public GameState apply(GameState state) {
        Position bridgePos = ptr.getPosition();
        Location bridgeLoc = ptr.getLocation();

        LinkedHashMap<Position, PlacedTile> placedTiles = state.getPlacedTiles();
        PlacedTile ptile = placedTiles.get(bridgePos).get();
        Rotation tileRotation = ptile.getRotation();
        ptile = ptile.mapTile(t -> t.addBridge(bridgeLoc.rotateCCW(tileRotation)));
        state = state.setPlacedTiles(placedTiles.put(bridgePos, ptile));

        Bridge bridge = new Bridge(bridgeLoc);
        Road bridgeRoad = bridge.placeOnBoard(bridgePos, Rotation.R0); // bridge itself is rotated, placed like tile has no rotation!

        for (Location side : bridgeLoc.splitToSides()) {
            Position adj = bridgePos.add(side);
            Feature f = state.getFeaturePartOf(adj, side.rev());
            if (f != null && !(f instanceof Road)) {
                // city gate
                bridgeRoad = bridgeRoad.closeEdge(new Edge(bridgePos, adj));
            }
        }

        Road _bridgeRoad = bridgeRoad;
        state = state.mapFeatureMap(m -> m.put(ptr.getPosition(), m.get(ptr.getPosition()).get().put(ptr, _bridgeRoad)));
        state = state.mapCapabilityModel(BridgeCapability.class, model -> model.add(ptr));

        if (!silent) {
            state = state.appendEvent(
                new TokenPlacedEvent(PlayEventMeta.createWithActivePlayer(state), BridgeToken.BRIDGE, ptr)
            );
        }

        return state;
    }

}
