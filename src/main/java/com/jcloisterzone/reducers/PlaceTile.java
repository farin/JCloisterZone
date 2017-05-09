package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TilePlacedEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.CompletableFeature;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class PlaceTile implements Reducer {

    private final TileDefinition tile;
    private final Position pos;
    private final Rotation rot;

    public PlaceTile(TileDefinition tile, Position pos, Rotation rot) {
        this.tile = tile;
        this.pos = pos;
        this.rot = rot;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public GameState apply(GameState state) {
        LinkedHashMap<Position, PlacedTile> placedTiles = state.getPlacedTiles();
        assert !placedTiles.containsKey(pos);
        boolean abbeyPlacement = TileDefinition.ABBEY_TILE_ID.equals(tile.getId());

        state = state.setPlacedTiles(
            placedTiles.put(pos, new PlacedTile(tile, pos, rot))
        );

        GameState _state = state;
        java.util.Map<FeaturePointer, Feature> fpUpdate = new java.util.HashMap<>();
        Stream.ofAll(tile.getInitialFeatures().values())
            .map(f -> f.placeOnBoard(pos, rot))
            .forEach(feature -> {
                if (feature instanceof MultiTileFeature) {
                    java.util.Set<Feature> alreadyMerged = new java.util.HashSet<>();
                    Stream<FeaturePointer> adjacent = feature.getPlaces().get().getAdjacent(feature.getClass());
                    feature = adjacent.foldLeft((MultiTileFeature) feature, (f, adjFp) -> {
                        MultiTileFeature adj = (MultiTileFeature) _state.getFeaturePartOf(adjFp);
                        if (adj == null || alreadyMerged.contains(adj)) return f;
                        alreadyMerged.add(adj);
                        return f.merge(adj);
                    });
                }
                for (FeaturePointer fp : feature.getPlaces()) {
                    fpUpdate.put(fp, feature);
                }
            });

        if (abbeyPlacement) {
            FeaturePointer abbeyFp = new FeaturePointer(pos, Location.CLOISTER);
            Set<FeaturePointer> abbeyNeighboring = HashSet.empty();
            for (Location side : Location.SIDES) {
                FeaturePointer adjPartOfPtr = new FeaturePointer(pos.add(side), side.rev());
                CompletableFeature<?> adj = (CompletableFeature) state.getFeaturePartOf(adjPartOfPtr);
                if (adj == null) {
                    //farm (or empty tile - which can happen only in debug when non-hole placement is enabled)
                    continue;
                }
                FeaturePointer adjPtr = adj.getPlaces().find(fp -> adjPartOfPtr.isPartOf(fp)).get();

                adj = adj.mergeAbbeyEdge(new Edge(pos, side));
                adj = adj.setNeighboring(adj.getNeighboring().add(abbeyFp));
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

        state = state.setFeatureMap(HashMap.ofAll(fpUpdate).merge(state.getFeatureMap()));
        state = state.appendEvent(
            new TilePlacedEvent(PlayEventMeta.createWithActivePlayer(state), tile, pos, rot)
        );
        for (Capability cap : state.getCapabilities().toSeq()) {
            state = cap.onTilePlaced(state);
        }
        return state;
    }

}
