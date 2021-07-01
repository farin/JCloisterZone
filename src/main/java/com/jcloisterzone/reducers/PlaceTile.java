package com.jcloisterzone.reducers;

import com.jcloisterzone.board.*;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.TilePlacedEvent;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlacedTunnelToken;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.*;

import java.util.ArrayList;

public class PlaceTile implements Reducer {

    private final Tile tile;
    private final Position pos;
    private final Rotation rot;

    // used once for whole placement
    private GameState _state;
    private java.util.Map<FeaturePointer, Feature> fpUpdate;
    private java.util.Set<FeaturePointer> newTunnels;
    private java.util.List<Tuple3<City, City, ShortEdge>> multiEdgePairsToMerge;

    // used for each feature merge
    private java.util.Set<Feature> alreadyMerged;
    private java.util.Set<Edge> mergedEdges;
    private java.util.List<Tuple3<Edge, EdgeFeature, FeaturePointer>> edgesToClose;


    public PlaceTile(Tile tile, Position pos, Rotation rot) {
        this.tile = tile;
        this.pos = pos;
        this.rot = rot;
    }

    private void findMultiEdges(City city) {
        // if multi-edge reference is no longer between open edges, another city was just merged this edge
        // and we need to merge third city there
        city.getMultiEdges()
        .filter(e -> mergedEdges.contains(e._1.toEdge()))
        .forEach(multiEdge -> {
            FeaturePointer fullFp = multiEdge._2;
            City adj = (City) getFeature(fullFp);
            multiEdgePairsToMerge.add(new Tuple3<>(adj, city, multiEdge._1));
        });
    }

    private EdgeFeature closeEdge(Edge edge, EdgeFeature f, FeaturePointer fp, EdgeFeature adj, FeaturePointer adjFp) {
        f = f.closeEdge(edge);

        if (adj != null && adj.getProxyTarget() != null) {
            adjFp = adj.getProxyTarget();
            adj = (EdgeFeature) getFeature(adjFp);
        }

        boolean neigbouring = f instanceof NeighbouringFeature && adj instanceof NeighbouringFeature;
        if (neigbouring) {
            NeighbouringFeature _f = (NeighbouringFeature) f;
            f = (EdgeFeature) _f.setNeighboring(_f.getNeighboring().add(adjFp));
        }

        if (f instanceof City) {
            ShortEdge shortEdge = new ShortEdge(fp.getPosition(), fp.getLocation());
            City city = (City) f;
            FeaturePointer multiEdgeFp = city.getMultiEdges().find(me -> me._1.equals(shortEdge)).map(t -> t._2).getOrNull();
            if (multiEdgeFp != null) {
                City city2 = (City) getFeature(multiEdgeFp);
                boolean sameCity = city2 == city;
                if (sameCity) {
                    f = city.setOpenEdges(city2.getOpenEdges().remove(shortEdge));
                } else {
                    city2 = city2.setOpenEdges(city2.getOpenEdges().remove(shortEdge));
                    if (adj instanceof NeighbouringFeature) {
                        city2 = city2.setNeighboring(city2.getNeighboring().add(adjFp));
                    }
                    updateRefs(city2);
                }
            }
        }

        return f;
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

        _state = state;
        fpUpdate = new java.util.HashMap<>();
        newTunnels = new java.util.HashSet<>();
        multiEdgePairsToMerge = new ArrayList<>();

        Stream.ofAll(tile.getInitialFeatures().values())
            .map(f -> f.placeOnBoard(pos, rot))
            .forEach(feature -> {
                // update TunnelCapability model
                if (feature instanceof Road) {
                    Road road = (Road) feature;
                    newTunnels.addAll(road.getOpenTunnelEnds().toJavaSet());
                }

                // merge features
                if (feature instanceof EdgeFeature) {
                    alreadyMerged = new java.util.HashSet<>();
                    mergedEdges = new java.util.HashSet<>();
                    edgesToClose = new ArrayList<>();

                    Stream<FeaturePointer> adjacent = feature.getPlaces().get().getAdjacent();
                    feature = adjacent.foldLeft((EdgeFeature) feature,  (f, adjFp) -> {
                        if (_state.getPlacedTile(adjFp.getPosition()) == null) {
                            // no adjacent tile
                            return f;
                        }

                    	// find adjacent feature part (already placed)
                        Tuple2<FeaturePointer, Feature> adjTuple = _state.getFeaturePartOf2(adjFp.getPosition(), adjFp.getLocation()); // search for any feature type
                        EdgeFeature adj = adjTuple == null ? null : (EdgeFeature) adjTuple._2;
                        if (adj != null && f.isMergeableWith(adj)) {
                            if (!alreadyMerged.contains(adj)) {
                                // adjacent tile is not empty and adjacent feature is not same as feature adjacent (already processed) by other side
                                alreadyMerged.add(adj); //still track original adj, because it is compared at the beginning of condition
                                f = ((MultiTileFeature)f).merge((MultiTileFeature) getRecent(adj));
                            }

                            if (f instanceof City) {
                                // this is needed to get correct open edges when merging HS.CC!.v tile) from two sided to same city
                                Edge edge = new Edge(pos, adjFp.getPosition());
                                mergedEdges.add(edge);
                            }
                        } else {
                            Edge edge = new Edge(pos, adjFp.getPosition());
                            edgesToClose.add(new Tuple3<>(edge, adj, adjFp));
                        }
                        return f;
                    });

                    // finally handle multi edge (Hills & Sheep HS.CC!.v tile)
                    if (feature instanceof City) {
                        this.findMultiEdges((City) feature);
                    }

                    for (var t : edgesToClose) {
                        Edge edge = t._1;
                        EdgeFeature adj = t._2;
                        FeaturePointer adjFp = t._3;
                        FeaturePointer otherFp = adjFp;

                        if (adj != null) adj = (EdgeFeature) getRecent(adj);

                        FeaturePointer fp = new FeaturePointer(adjFp.getPosition().add(adjFp.getLocation()), feature.getClass(), adjFp.getLocation().rev());
                        feature = closeEdge(edge, (EdgeFeature) feature, fp, adj, adjFp);

                        if (adj != null) {
                            // TODO test against bridge
                            adj = closeEdge(edge, adj, adjFp, (EdgeFeature) feature, fp);
                            updateRefs(adj);
                        }
                    }
                }

                updateRefs(feature);
            });

        // merge hills and sheep multi edge after all normal merges are processed
        for (Tuple3<City, City, ShortEdge> t: multiEdgePairsToMerge) {
            City c1 = (City) getRecent(t._1);
            City c2 = (City) getRecent(t._2);
            City c = c1 == c2 ? c1 : c1.merge(c2);
            c = c.setOpenEdges(c.getOpenEdges().remove(t._3));
            updateRefs(c);
        }

        if (!newTunnels.isEmpty()) {
            state = state.mapCapabilityModel(TunnelCapability.class, model -> {
                Map<FeaturePointer, PlacedTunnelToken> newTunnelsMap = HashSet.ofAll(newTunnels).toMap(fp -> new Tuple2<>(fp, null));
                return model.merge(newTunnelsMap);
            });
        }

        state = state.updateFeatureMap(HashMap.ofAll(fpUpdate));
        state = state.appendEvent(
            new TilePlacedEvent(PlayEventMeta.createWithActivePlayer(state), tile, pos, rot)
        );
        for (Capability cap : state.getCapabilities().toSeq()) {
            state = cap.onTilePlaced(state, placedTile);
        }
        return state;
    }

    private Feature getFeature(FeaturePointer fp) {
        Feature f = fpUpdate.get(fp);
        return f == null ? _state.getFeature(fp) : f;
    }

    private Feature getRecent(Feature f) {
        Feature updated = fpUpdate.get(f.getPlaces().get());
        return updated == null ? f : updated;
    }

    private void updateRefs(Feature f) {
        for (FeaturePointer fp : f.getPlaces()) {
            fpUpdate.put(fp, f);
        }
    }
}
