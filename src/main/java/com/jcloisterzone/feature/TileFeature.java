package com.jcloisterzone.feature;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

@Immutable
public abstract class TileFeature implements Feature, Serializable {

    private static final long serialVersionUID = 1L;

    protected final List<FeaturePointer> places;

    public TileFeature(List<FeaturePointer> places) {
        this.places = places;
    }

    @Override
    public List<FeaturePointer> getPlaces() {
       return places;
    }

    public FeaturePointer getPlace() {
        return places.get();
    }

    @Override
    public Stream<Tuple2<Meeple, FeaturePointer>> getMeeples2(GameState state) {
        Set<FeaturePointer> fps = HashSet.ofAll(places);
        return Stream.ofAll(state.getDeployedMeeples())
            .filter(t -> fps.contains(t._2));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static String getLocalizedNamefor(Class<? extends Feature> feature) {
        try {
            Method m = feature.getMethod("name");
            return (String) m.invoke(null);
        } catch (Exception e) {
            return feature.getSimpleName();
        }
    }


    // immutable helpers

    protected List<FeaturePointer> mergePlaces(TileFeature obj) {
        return this.places.appendAll(obj.places);
    }

    protected List<FeaturePointer> placeOnBoardPlaces(Position pos, Rotation rot) {
        return this.places.map(fp -> fp.rotateCW(rot).translate(pos));
    }

}
