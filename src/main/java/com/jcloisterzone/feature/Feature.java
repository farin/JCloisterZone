package com.jcloisterzone.feature;


import java.lang.reflect.Method;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;
import io.vavr.collection.Set;

public interface Feature {

    List<FeaturePointer> getPlaces();
    Feature placeOnBoard(Position pos, Rotation rot);

    default Set<Position> getTilePositions() {
        return getPlaces().map(fp -> fp.getPosition()).toSet();
    }

    static String getLocalizedNamefor(Class<? extends Feature> feature) {
        try {
            Method m = feature.getMethod("name");
            return (String) m.invoke(null);
        } catch (Exception e) {
            return feature.getSimpleName();
        }
    }
}
