package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

import java.awt.*;
import java.util.function.Function;

public interface BoardMixin {

    LinkedHashMap<Position, PlacedTile> getPlacedTiles();
    GameState setPlacedTiles(LinkedHashMap<Position, PlacedTile> placedTiles);

    Map<Position, Map<FeaturePointer, Feature>> getFeatureMap();
    GameState setFeatureMap(Map<Position, Map<FeaturePointer, Feature>> featureMap);

    default GameState mapFeatureMap(Function<Map<Position, Map<FeaturePointer, Feature>>, Map<Position, Map<FeaturePointer, Feature>>> fn) {
        return setFeatureMap(fn.apply(getFeatureMap()));
    }

    default GameState updateFeatureMap(Map<FeaturePointer, Feature> fpUpdate) {
        return this.mapFeatureMap(m -> {
            for (var t : fpUpdate) {
                FeaturePointer fp = t._1;
                Position pos = fp.getPosition();
                m = m.put(pos, m.get(pos).getOrElse(HashMap.empty()).put(fp, t._2));
            }
            return m;
        });
    };

    default GameState putFeature(FeaturePointer fp, Feature feature) {
        return this.mapFeatureMap(m -> {
            Position pos = fp.getPosition();
            return m.put(pos, m.get(pos).getOrElse(HashMap.empty()).put(fp, feature));
        });
    }

    // Tiles

    default PlacedTile getPlacedTile(Position pos) {
        return getPlacedTiles().get(pos).getOrNull();
    }

    default PlacedTile getLastPlaced() {
        return getPlacedTiles().takeRight(1).map(Tuple2::_2).getOrNull();
    }

    default Stream<Tuple2<Location, PlacedTile>> getAdjacentTiles2(Position pos) {
        return Stream.ofAll(Position.ADJACENT)
            .map(locPos-> locPos.map2(
                offset -> getPlacedTile(pos.add(offset))
            ))
            .filter(locTile -> locTile._2 != null);
    }

    default Stream<PlacedTile> getAdjacentTiles(Position pos) {
        return getAdjacentTiles2(pos).map(Tuple2::_2);
    }

    default  Stream<Tuple2<Location, PlacedTile>> getAdjacentAndDiagonalTiles2(Position pos) {
        return Stream.ofAll(Position.ADJACENT_AND_DIAGONAL)
            .map(locPos-> locPos.map2(
                offset -> getPlacedTile(pos.add(offset))
            ))
            .filter(locTile -> locTile._2 != null);
    }

    default Stream<PlacedTile> getAdjacentAndDiagonalTiles(Position pos) {
        return getAdjacentAndDiagonalTiles2(pos).map(Tuple2::_2);
    }


    default Rectangle getBoardBounds() {
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        for (Position pos : getPlacedTiles().keySet()) {
            if (minX > pos.x) minX = pos.x;
            if (maxX < pos.x) maxX = pos.x;
            if (minY > pos.y) minY = pos.y;
            if (maxY < pos.y) maxY = pos.y;
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    // Features
    default Stream<Feature> getFeatures() {
        return Stream.ofAll(getFeatureMap().values()).flatMap(m -> m.values()).distinct();
    }

    default <T extends Feature> Stream<T> getFeatures(Class<T> cls) {
        return getFeatures()
                .filter(Predicates.instanceOf(cls))
                .distinct()
                .map(f -> (T) f);
    }

    default Stream<Tuple2<FeaturePointer, Feature>> getTileFeatures2(Position pos) {
        return getFeatureMap().get(pos).getOrElse(HashMap.empty()).toStream();
    }

    @SuppressWarnings("unchecked")
    default <T extends Feature> Stream<Tuple2<FeaturePointer, T>> getTileFeatures2(Position pos, Class<T> cls) {
        return getTileFeatures2(pos)
           .filter(t -> cls.isInstance(t._2))
           .map(t -> (Tuple2<FeaturePointer, T>) t);
    }

    default Feature getFeature(FeaturePointer fp) {
        if (fp.getLocation() == Location.AS_ABBOT) fp = fp.setLocation(Location.I);
        var tileMap = getFeatureMap().get(fp.getPosition()).getOrNull();
        return tileMap == null ? null : tileMap.get(fp).getOrNull();
    }

    default Structure getStructure(FeaturePointer fp) {
        Feature f = getFeature(fp);
        return f instanceof Structure ? (Structure) f : null;
    }

    default Feature getFeaturePartOf(Position pos, Location loc) {
        if (loc == Location.AS_ABBOT) loc = Location.I;
        var t = getPlacedTile(pos).getInitialFeaturePartOf(loc);
        FeaturePointer fp = t._1.setPosition(pos);
        var tileMap = getFeatureMap().get(pos).getOrElse(HashMap.empty());
        Feature f = tileMap.get(fp).getOrNull();
        if (f == null && fp.getFeature().equals(City.class)) {
            f = tileMap.get(fp.setFeature(Castle.class)).getOrNull();
        }
        return f;
    }

    default Tuple2<FeaturePointer, Feature> getFeaturePartOf2(Position pos, Location loc) {
        if (loc == Location.AS_ABBOT) loc = Location.I;
        var t = getPlacedTile(pos).getInitialFeaturePartOf(loc);
        var feature =   getFeatureMap().get(pos).getOrElse(HashMap.empty()).get(t._1.setPosition(pos)).getOrNull();
        return feature == null ? null : new Tuple2<>(t._1.setPosition(pos), feature);
    }

    default Feature getFeaturePartOf(FeaturePointer fp) {
        FeaturePointer normFp = fp.getLocation() == Location.AS_ABBOT ? fp.setLocation(Location.I) : fp;
        return getFeatureMap()
                .get(fp.getPosition()).getOrElse(HashMap.empty())
                .find(t -> normFp.isPartOf(t._1))
                .map(Tuple2::_2)
                .getOrNull();
    }

    default Structure getStructurePartOf(FeaturePointer fp) {
        Feature f = getFeaturePartOf(fp);
        return f instanceof Structure ? (Structure) f : null;
    }

    /** Returns Tuple2 with feature and "full" feature pointer.
     */
    default Tuple2<FeaturePointer, Feature> getFeaturePartOf2(FeaturePointer fp) {
        FeaturePointer normFp = fp.getLocation() == Location.AS_ABBOT ? fp.setLocation(Location.I) : fp;
        return getFeatureMap()
                .get(fp.getPosition()).getOrElse(HashMap.empty())
                .find(t -> normFp.isPartOf(t._1))
                .getOrNull();
    }
}
