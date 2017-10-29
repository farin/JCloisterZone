package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;

public class River extends TileFeature {

    public River(List<FeaturePointer> places) {
        super(places);
    }

    @Override
    public River placeOnBoard(Position pos, Rotation rot) {
        return new River(
            placeOnBoardPlaces(pos, rot)
        );
    }

}
