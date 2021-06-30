package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.Set;

/** neigbouring for C1 Wagon move purpose */
public interface NeighbouringFeature extends Feature {

    NeighbouringFeature setNeighboring(Set<FeaturePointer> neighboring);
    Set<FeaturePointer> getNeighboring();

    default Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return getNeighboring().map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
