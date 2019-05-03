package com.jcloisterzone.ui.resources;

import java.awt.Image;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.ImmutablePoint;

public interface ResourceManager {

    int NORMALIZED_SIZE = 1000;
    int POINT_NORMALIZED_SIZE = 100; // TODO merge with NORMALIZED_SIZE

    TileImage getTileImage(String tileId, Rotation rot); //use custom rotation

    //generic image, path is without extension
    Image getImage(String path);
    Image getLayeredImage(LayeredImageDescriptor lid);

    FeatureArea getFeatureArea(String effectiveTileId, Tile tile, Rotation rot, Location loc);
    FeatureArea getBarnArea();
    FeatureArea getBridgeArea(Location bridgeLocation);

    //TODO change to 1000x1000
    /** returns meeple offset on tile, normalized to 100x100 tile size */
    ImmutablePoint getMeeplePlacement(String effectiveTileId, Tile tile, Rotation rot, Location loc);
    ImmutablePoint getBarnPlacement();

    void reload();

    default FeatureArea getFeatureArea(Tile tile, Rotation rot, Location loc) {
        return getFeatureArea(tile.getId(), tile, rot, loc);
    }

    default ImmutablePoint getMeeplePlacement(Tile tile, Rotation rot, Location loc) {
        return getMeeplePlacement(tile.getId(), tile, rot, loc);
    }
}
