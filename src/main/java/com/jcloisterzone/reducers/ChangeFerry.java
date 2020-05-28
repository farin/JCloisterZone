package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.game.capability.FerriesCapabilityModel;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class ChangeFerry implements Reducer {

    private final FeaturePointer from;
    private final FeaturePointer to;

    public ChangeFerry(FeaturePointer from, FeaturePointer to) {
        assert from.getPosition().equals(to.getPosition());
        this.from = from;
        this.to = to;
    }

    @Override
    public GameState apply(GameState state) {
        state = removeFerry(state);
        state = (new PlaceFerry(to)).apply(state);

        List<FeaturePointer> disconnectedParts = from.getLocation().subtract(to.getLocation()).splitToSides()
                .map(loc -> new FeaturePointer(from.getPosition(), loc));

        for (FeaturePointer fp: disconnectedParts) {
            Structure feature = state.getStructure(fp);
            Stream<Tuple2<Meeple, FeaturePointer>> threatened = feature.getMeeples2(state)
                .filter(m -> m._1 instanceof Builder);

            for (Tuple2<Meeple, FeaturePointer> t : threatened) {
                if (feature.getFollowers(state).find(f -> f.getPlayer().equals(t._1.getPlayer())).isEmpty()) {
                    state = new UndeployMeeple(t._1, false).apply(state);
                }
            }
        }
        return state;
    }

    private GameState removeFerry(GameState state) {
        state = state.mapCapabilityModel(FerriesCapability.class, m -> {
            return new FerriesCapabilityModel(m.getFerries().remove(from), m.getMovedFerries());
        });

        List<FeaturePointer> sides = from.getLocation().splitToSides().map(loc -> from.setLocation(loc));
        Road merged = (Road) state.getFeature(sides.get());

        GameState _state = state;
        List<Road> parts = sides.map(side -> {
           java.util.Set<FeaturePointer> placesSet = new java.util.HashSet<>();
           merged.findNearest(_state, side, fp -> {
               placesSet.add(fp);
               return false; //never stop, go through connected parts and collect feature pointers
           });
           List<FeaturePointer> places = List.ofAll(placesSet);
           List<Road> initialFeatures = places
                   .map(fp -> {
                        PlacedTile pt = _state.getPlacedTile(fp.getPosition());
                        return (Road) pt.getInitialFeaturePartOf(fp.getLocation())
                            .placeOnBoard(fp.getPosition(), pt.getRotation());
                   });
           boolean isInn = initialFeatures.foldLeft(false, (res, road) -> res || road.isInn());
           boolean isLabyrinth = initialFeatures.foldLeft(false, (res, road) -> res || road.isLabyrinth());
           Set<FeaturePointer> openTunnelEnds = merged.getOpenTunnelEnds().intersect(places.toSet());
           Set<Edge> openEdges = merged.getOpenEdges().intersect(
               initialFeatures.flatMap(f -> f.getOpenEdges()).toSet()
           );
           Set<FeaturePointer> neighbouring = merged.getNeighboring().intersect(
               initialFeatures.flatMap(f -> f.getNeighboring()).toSet()
           );
           return new Road(places, openEdges, neighbouring, isInn, isLabyrinth, openTunnelEnds);
        });

        // handle special case, ferry connected two ends of same road (after disconnect)
        // mind that such can be still open (eg. using tile with the well from Abbey&Mayor (unclosed T road))
        // in such case, nothing changes for the road
        if (parts.get(0).getPlaces().size() != merged.getPlaces().size()) {
            for (Road part : parts) {
                state = state.mapFeatureMap(m ->
                    part.getPlaces()
                        .toMap(fp -> new Tuple2<FeaturePointer, Feature>(fp, part))
                        .merge(m)
                );
            }
        }

        return state;
    }


}
