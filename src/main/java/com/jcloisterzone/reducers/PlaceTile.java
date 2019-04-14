package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TilePlacedEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlacedTunnelToken;

import io.vavr.Function3;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class PlaceTile implements Reducer {

    private final Tile tile;
    private final Position pos;
    private final Rotation rot;

    public PlaceTile(Tile tile, Position pos, Rotation rot) {
        this.tile = tile;
        this.pos = pos;
        this.rot = rot;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public GameState apply(GameState state) {
        LinkedHashMap<Position, PlacedTile> placedTiles = state.getPlacedTiles();
        assert !placedTiles.containsKey(pos);
        boolean abbeyPlacement = AbbeyCapability.isAbbey(tile);

        PlacedTile placedTile = new PlacedTile(tile, pos, rot);
        state = state.setPlacedTiles(
            placedTiles.put(pos, placedTile)
        );

        GameState _state = state;
        java.util.Map<FeaturePointer, Feature> fpUpdate = new java.util.HashMap<>();
        java.util.Set<FeaturePointer> newTunnels = new java.util.HashSet<>();
        Stream.ofAll(tile.getInitialFeatures().values())
            .map(f -> f.placeOnBoard(pos, rot))
            .forEach(feature -> {
                // update TunnelCapability model
                if (feature instanceof Road) {
                    Road road = (Road) feature;
                    newTunnels.addAll(road.getOpenTunnelEnds().toJavaSet());
                }

                // merge features
                if (feature instanceof MultiTileFeature) {
                    java.util.Set<Feature> alreadyMerged = new java.util.HashSet<>();

                    Function3<MultiTileFeature, FeaturePointer, MultiTileFeature, MultiTileFeature> merge = (f, fullFp, adj) -> {
                        // if needs merge, check if state contains recent feature version
                        MultiTileFeature updatedAdj = adj;
                        if (fpUpdate.containsKey(fullFp)) { // "full" feature pointer from adjTuple must be used instead of "part of" adjFp
                            // not recent - adj is going to be replaced by previous iteration of forEach
                            // (separated feature on the placed tile is merging to same adjacent feature
                            // Eg. road crossing is closing road ring
                            //         |
                            //     ....|....+....
                            //    .    |    .      <<< just placed
                            //    .    |    .
                            // --------+---------
                            //    .    |    .
                            //    .    |    .
                            //     ....|....
                            //         |
                        	updatedAdj = (MultiTileFeature) fpUpdate.get(fullFp);
                        }
                        alreadyMerged.add(adj); //still track original adj, because it is compared at the beginning of condition
                        return f.merge(updatedAdj);
                    };

                    Stream<FeaturePointer> adjacent = feature.getPlaces().get().getAdjacent(feature.getClass());
                    feature = adjacent.foldLeft((MultiTileFeature) feature,  (f, adjFp) -> {
                    	// find adjacent feature part (already placed)
                        Tuple2<FeaturePointer, Feature> adjTuple = _state.getFeaturePartOf2(adjFp);
                        MultiTileFeature adj = adjTuple == null ? null : (MultiTileFeature) adjTuple._2;
                        if (adj == null || alreadyMerged.contains(adj)) {
                            // adjacent tile is empty or adjacent feature is same as feature adjacent (already processed) to other side
                            return f;
                        }
                    	return merge.apply(f, adjTuple._1, adj);
                    });

                    // finally handle mutli-tile edge (Hills & Sheep HS.CC!.v tile)
                    if (feature instanceof City) {
                    	City city = (City) feature;
                    	Set<Edge> openEdges = city.getOpenEdges();
                    	// if mutli-edge reference is no longer between open edges, another city was just merged this edge
                    	// and we need to merge third city there
                    	Set<Tuple2<Edge, FeaturePointer>> mutliEdgeToMerge = city.getMultiEdges().filter(e -> !openEdges.contains(e._1));

//                    	System.err.println("------------");
//                    	System.err.println(openEdges);
//                    	System.err.println(city.getMultiEdges());
//                    	System.err.println(mutliEdgeToMerge);

                    	for (Tuple2<Edge, FeaturePointer> multiEdge : mutliEdgeToMerge) {
                    		FeaturePointer fullFp = multiEdge._2;
                    		City adj = (City) _state.getFeature(fullFp);

                    		if (!alreadyMerged.contains(adj)) {
                    			city = (City) merge.apply(city, fullFp, adj);
                    		}
                    	}

                    	city = city.setMultiEdges(city.getMultiEdges().removeAll(mutliEdgeToMerge));
                    	city = city.setOpenEdges(city.getOpenEdges().removeAll(mutliEdgeToMerge.map(Tuple2::_1)));
                    	feature = city;
                    }
                }
                for (FeaturePointer fp : feature.getPlaces()) {
                    fpUpdate.put(fp, feature);
                }
            });

        if (abbeyPlacement) {
            java.util.Map<CompletableFeature<?>, CompletableFeature<?>> featureReplacement = new java.util.HashMap<>();
            FeaturePointer abbeyFp = new FeaturePointer(pos, Location.CLOISTER);
            Set<FeaturePointer> abbeyNeighboring = HashSet.empty();
            for (Location side : Location.SIDES) {
                FeaturePointer adjPartOfPtr = new FeaturePointer(pos.add(side), side.rev());
                CompletableFeature<?> adj = (CompletableFeature) state.getFeaturePartOf(adjPartOfPtr);
                if (adj == null) {
                    //farm (or empty tile - which can happen only in debug when non-hole placement is enabled)
                    continue;
                }
                CompletableFeature<?> originalAdj = adj;
                if (featureReplacement.get(originalAdj) != null) {
                    // when same feature is merged on multiple abbey side, then use update feature objects
                    // to not lost partial changes
                    adj = featureReplacement.get(originalAdj);
                }
                FeaturePointer adjPtr = adj.getPlaces().find(fp -> adjPartOfPtr.isPartOf(fp)).get();

                adj = adj.mergeAbbeyEdge(new Edge(pos, side));
                adj = adj.setNeighboring(adj.getNeighboring().add(abbeyFp));
                featureReplacement.put(originalAdj, adj);
                for (FeaturePointer fp : adj.getPlaces()) {
                    fpUpdate.put(fp, adj);
                }
                abbeyNeighboring = abbeyNeighboring.add(adjPtr);
            }
            if (!abbeyNeighboring.isEmpty()) {
                Cloister abbey = (Cloister) fpUpdate.get(abbeyFp);
                fpUpdate.put(abbeyFp, abbey.setNeighboring(abbeyNeighboring));
            }
        }

        if (!newTunnels.isEmpty()) {
            state = state.mapCapabilityModel(TunnelCapability.class, model -> {
                Map<FeaturePointer, PlacedTunnelToken> newTunnelsMap = HashSet.ofAll(newTunnels).toMap(fp -> new Tuple2<>(fp, null));
                return model.merge(newTunnelsMap);
            });
        }

        state = state.mapFeatureMap(m -> HashMap.ofAll(fpUpdate).merge(m));
        state = state.appendEvent(
            new TilePlacedEvent(PlayEventMeta.createWithActivePlayer(state), tile, pos, rot)
        );
        for (Capability cap : state.getCapabilities().toSeq()) {
            state = cap.onTilePlaced(state, placedTile);
        }
        return state;
    }

}
