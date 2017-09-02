package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;

public class Quarter extends TileFeature implements Structure {

    private static final long serialVersionUID = 1L;

    //final Class<? extends Feature> targetFeature;

    public Quarter(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        throw new UnsupportedOperationException();
    }
}
