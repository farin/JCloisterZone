package com.jcloisterzone.feature;

import java.util.List;

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

    void addMeeple(Meeple meeple);
    void removeMeeple(Meeple meeple);
    List<Meeple> getMeeples();

    <T> T walk(FeatureVisitor<T> visitor);
    /**
     * Returns feature part with minimal ID.
     */
    Feature getMaster();

}
