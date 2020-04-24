package com.jcloisterzone.reducers;

import com.jcloisterzone.board.*;
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
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

import javax.lang.model.type.ArrayType;
import java.util.ArrayList;

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
        java.util.List<Tuple3<FeaturePointer, FeaturePointer, ShortEdge>> multiEdgePairsToMerge = new ArrayList<>();

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
                    java.util.Set<Edge> mergedEdges = new java.util.HashSet<>();

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
                        if (adj != null) {
                            if (!alreadyMerged.contains(adj)) {
                                // adjacent tile is not empty and adjacent feature is not same as feature adjacent (already processed) to other side
                                f = merge.apply(f, adjTuple._1, adj);
                            }

                            if (f instanceof City) {
                                // this is needed to get correct open edges when merging HS.CC!.v tile) from two sided to same city
                                Edge edge = new Edge(pos, adjFp.getPosition());
                                mergedEdges.add(edge);
                            }

                        }
                        return f;
                    });

                    // finally handle mutli-tile edge (Hills & Sheep HS.CC!.v tile)
                    if (feature instanceof City) {
                    	City city = (City) feature;
                    	Set<Edge> openEdges = city.getOpenEdges();
                    	// if mutli-edge reference is no longer between open edges, another city was just merged this edge
                    	// and we need to merge third city there
                    	Set<Tuple2<ShortEdge, FeaturePointer>> mutliEdgeToMerge = city.getMultiEdges()
                                .filter(e -> mergedEdges.contains(e._1.toEdge()));

                    	if (mutliEdgeToMerge.nonEmpty()) {
                            for (Tuple2<ShortEdge, FeaturePointer> multiEdge : mutliEdgeToMerge) {
                                FeaturePointer fullFp = multiEdge._2;
                                City adj = (City) _state.getFeature(fullFp);

                                // finding feature pointer on pos is technically not necessary, any place can be used, but let it clear
                                multiEdgePairsToMerge.add(new Tuple3<FeaturePointer, FeaturePointer, ShortEdge>(
                                        fullFp,
                                        feature.getPlaces().find(fp -> fp.getPosition().equals(pos)).get(),
                                        multiEdge._1
                                ));
                                // if is not updated yet put it there anyway to make bellow statetement fpUpdate.get(t._1) works in any case
                                if (!fpUpdate.containsKey(fullFp)) {
                                    fpUpdate.put(fullFp, adj);
                                }
                            }

                            feature = city;
                        }
                    }
                }
                updateRefs(fpUpdate, feature);
            });

        // merge hills and sheep multi edge after all normal merges are processed
        for (Tuple3<FeaturePointer, FeaturePointer, ShortEdge> t: multiEdgePairsToMerge) {
            City c1 = (City) fpUpdate.get(t._1);
            City c2 = (City) fpUpdate.get(t._2);
            City c = c1 == c2 ? c1 : c1.merge(c2);

            c = c.setOpenEdges(c.getOpenEdges().remove(t._3));
            updateRefs(fpUpdate, c);
        }

        if (abbeyPlacement) {
            // Abbey is always just placed tile
            // it's not possible to be adjacent to placed tile because it abbey can be placed only to existing hole.
            java.util.Map<CompletableFeature<?>, CompletableFeature<?>> featureReplacement = new java.util.HashMap<>();
            FeaturePointer abbeyFp = new FeaturePointer(pos, Location.CLOISTER);
            Set<FeaturePointer> abbeyNeighboring = HashSet.empty();
            for (Location side : Location.SIDES) {
                FeaturePointer adjPartOfPtr = new FeaturePointer(pos.add(side), side.rev());
                CompletableFeature<?> originalAdj = (CompletableFeature) state.getFeaturePartOf(adjPartOfPtr);
                if (originalAdj == null) {
                    //farm (or empty tile - which can happen only in debug when non-hole placement is enabled)
                    continue;
                }

                // when same feature is merged on multiple abbey sides, then use update feature objects
                // to not lost partial changes
                CompletableFeature<?> adj = featureReplacement.getOrDefault(originalAdj, originalAdj);
                FeaturePointer adjPtr = adj.getPlaces().find(fp -> adjPartOfPtr.isPartOf(fp)).get();

                adj = adj.mergeAbbeyEdge(new Edge(pos, side));
                adj = adj.setNeighboring(adj.getNeighboring().add(abbeyFp));

                if (adj instanceof City) {
                    ShortEdge edge = new ShortEdge(pos, side);
                    City city = (City) adj;
                    Tuple2<ShortEdge, FeaturePointer> multiEdge = city.getMultiEdges().find(me -> me._1.equals(edge)).getOrNull();
                    if (multiEdge != null) {
                        FeaturePointer multiEdgeFp = multiEdge._2;
                        City originalCity2 = (City) state.getFeaturePartOf(multiEdgeFp);
                        City city2 = (City) featureReplacement.getOrDefault(originalCity2, originalCity2);

                        if (originalAdj == originalCity2) {
                            adj = adj.setOpenEdges(adj.getOpenEdges().remove(multiEdge._1));
                        } else {
                            city2 = city2.setOpenEdges(city2.getOpenEdges().remove(multiEdge._1));
                            city2 = city2.setNeighboring(city2.getNeighboring().add(abbeyFp));

                            featureReplacement.put(originalCity2, city2);
                            updateRefs(fpUpdate, city2);
                            abbeyNeighboring = abbeyNeighboring.add(multiEdgeFp);
                        }
                    }
                }

                featureReplacement.put(originalAdj, adj);
                updateRefs(fpUpdate, adj);
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

    private void updateRefs(java.util.Map<FeaturePointer, Feature> fpUpdate, Feature f) {
        for (FeaturePointer fp : f.getPlaces()) {
            fpUpdate.put(fp, f);
        }
    }

}
