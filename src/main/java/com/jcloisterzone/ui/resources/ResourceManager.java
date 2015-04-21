package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

public interface ResourceManager {

     Image getTileImage(Tile tile);
     Image getAbbeyImage();

     Map<Location, Area> getFeatureAreas(Tile tile, int size, Set<Location> locations);
     Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners);
     Map<Location, Area> getBridgeAreas(Tile tile, int size, Set<Location> locations);

     //TODO change to 1000x1000
     /** returns meeple offset on tile, normalized to 100x100 tile size */
     ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc);







}
