package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTunnelToken;
import io.vavr.Tuple2;
import io.vavr.collection.Map;

public class PlaceTunnel implements Reducer {

    private final Tunnel token;
    private final FeaturePointer ptr;

    public PlaceTunnel(Tunnel token, FeaturePointer ptr) {
        super();
        this.token = token;
        this.ptr = ptr;
    }

    @Override
    public GameState apply(GameState state) {
        Player player = state.getActivePlayer();
        PlacedTunnelToken placedToken = new PlacedTunnelToken(player.getIndex(), token);
        Map<FeaturePointer, PlacedTunnelToken> tunnels = state.getCapabilityModel(TunnelCapability.class);
        FeaturePointer secondEnd = tunnels
            .find(t -> t._2 != null && t._2.equals(placedToken))
            .map(Tuple2::_1)
            .getOrNull();

        if (secondEnd != null) {
            Road r1 = (Road) state.getFeature(ptr);
            Road r2 = (Road) state.getFeature(secondEnd);
            Road merged = r1.connectTunnels(r2, ptr, secondEnd);
            Map<FeaturePointer, Feature> featureMapUpdate = merged
                .getPlaces()
                .toMap(fp -> new Tuple2<>(fp, merged));
            state = state.mapFeatureMap(m -> featureMapUpdate.merge(m));
        }

        state = state.addFlag(Flag.TUNNEL_PLACED);
        state = state.setCapabilityModel(TunnelCapability.class, tunnels.put(ptr, placedToken));
        state = state.appendEvent(new TokenPlacedEvent(PlayEventMeta.createWithActivePlayer(state), token, ptr));
        return state;
    }

}
