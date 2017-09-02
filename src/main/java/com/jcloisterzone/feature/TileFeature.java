package com.jcloisterzone.feature;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;

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
    public String toString() {
        return getClass().getSimpleName();
    }

    // immutable helpers

    protected List<FeaturePointer> mergePlaces(TileFeature obj) {
        return this.places.appendAll(obj.places);
    }

    protected List<FeaturePointer> placeOnBoardPlaces(Position pos, Rotation rot) {
        return this.places.map(fp -> fp.rotateCW(rot).translate(pos));
    }

}
