package com.jcloisterzone.ui.resources;

import java.awt.Image;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.ImmutablePoint;

public interface ResourceManager {

    static final int NORMALIZED_SIZE = 1000;
    static final int POINT_NORMALIZED_SIZE = 100; // TODO merge with NORMALIZED_SIZE

    TileImage getTileImage(Tile tile, Rotation rot); //use custom rotation
    TileImage getAbbeyImage(Rotation rot);

    //generic image, path is without extension
    Image getImage(String path);
    Image getLayeredImage(LayeredImageDescriptor lid);

    FeatureArea getFeatureArea(Tile tile, Rotation rot, Location loc);
    FeatureArea getBarnArea();
    FeatureArea getBridgeArea(Location bridgeLocation);

    //TODO change to 1000x1000
    /** returns meeple offset on tile, normalized to 100x100 tile size */
    ImmutablePoint getMeeplePlacement(Tile tile, Rotation rot, Location loc);
    ImmutablePoint getBarnPlacement();
}
