package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class CityGate extends TileFeature implements NeighbouringFeature {

    protected final Set<FeaturePointer> neighboring; //for wagon move

    public CityGate(List<FeaturePointer> places) {
        this(places, HashSet.empty());
    }

    public CityGate(List<FeaturePointer> places, Set<FeaturePointer> neighboring) {
        super(places);
        this.neighboring = neighboring;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new CityGate(placeOnBoardPlaces(pos, rot), placeOnBoardNeighboring(pos, rot));
    }

    @Override
    public CityGate setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new CityGate(places, neighboring);
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }
}
