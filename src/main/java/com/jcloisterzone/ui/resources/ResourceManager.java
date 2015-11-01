package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

public interface ResourceManager {


    Image getTileImage(Tile tile);  //take rotation from tile instance
    Image getTileImage(Tile tile, Rotation rot); //use custom rotation
    Image getAbbeyImage(Rotation rot);

    Map<Location, FeatureArea> getFeatureAreas(Tile tile, int width, int height, Set<Location> locations);
    Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int width, int height, Set<Location> corners);
    Map<Location, FeatureArea> getBridgeAreas(Tile tile, int width, int height, Set<Location> locations);

    //TODO change to 1000x1000
    /** returns meeple offset on tile, normalized to 100x100 tile size */
    ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc);
}
