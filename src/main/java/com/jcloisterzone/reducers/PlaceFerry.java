package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.game.capability.FerriesCapability.FerryToken;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.List;

public class PlaceFerry implements Reducer {

    private final FeaturePointer ferry;

    public PlaceFerry(FeaturePointer ferry) {
        this.ferry = ferry;
    }

    @Override
    public GameState apply(GameState state) {
        GameState _state = state;

        state = state.mapCapabilityModel(FerriesCapability.class, m -> m.addFerry(ferry));

        List<Road> ends = ferry.getLocation().splitToSides().map(l -> (Road) _state.getFeature(ferry.setLocation(l)));
        assert ends.size() == 2;
        Road r1 = ends.get(0);
        Road r2 = ends.get(1);
        if (r1 != r2) {
            Road merged = r1.merge(r2);
            state = state.mapFeatureMap(m ->
                merged.getPlaces()
                    .toMap(fp -> new Tuple2<FeaturePointer, Feature>(fp, merged))
                    .merge(m)
            );
        }

        state = state.appendEvent(
            new TokenPlacedEvent(PlayEventMeta.createWithActivePlayer(state), FerryToken.FERRY, ferry)
        );
        return state;
    }

}
