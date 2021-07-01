package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;

public class CityGate extends TileFeature implements EdgeFeature<CityGate> {

    private FeaturePointer adjoiningCity;

    public CityGate(List<FeaturePointer> places, FeaturePointer adjoiningCity) {
        super(places);
        this.adjoiningCity = adjoiningCity;
    }

    @Override
    public boolean isMergeableWith(EdgeFeature<?> other) {
        return false;
    }

    @Override
    public CityGate closeEdge(Edge edge) {
        return this;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new CityGate(placeOnBoardPlaces(pos, rot), adjoiningCity.rotateCW(rot).translate(pos));
    }

    public FeaturePointer getAdjoiningCity() {
        return adjoiningCity;
    }
}
