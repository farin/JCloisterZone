package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Meeple;

public interface Feature {

    int getId();

    Location getLocation();
    Location getRawLocation();
    Tile getTile();
    Feature[] getNeighbouring();

    void setMeeple(Meeple meeple);
    Meeple getMeeple();

    <T> T walk(FeatureVisitor<T> visitor);
    /**
     * Returns feature part with minimal ID.
     */
    Feature getMaster();

}
