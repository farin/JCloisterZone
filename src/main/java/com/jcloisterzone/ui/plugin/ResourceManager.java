package com.jcloisterzone.ui.plugin;

import java.awt.Image;
import java.awt.geom.Area;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

public interface ResourceManager {

     Image getTileImage(String tileId);
     Area getFeatureArea(String tileId, Feature piece, Location loc);

     //TODO use tile id instead! API revision needed
     ImmutablePoint getFigurePlacement(Tile tile, Meeple m);
     ImmutablePoint getFigurePlacement(Tile tile, Class<? extends Feature> piece, Location loc);
     Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners);
     Area getBridgeArea(int size, Location loc);
     Map<Location, Area> getBridgeAreas(int size, Set<Location> locations);
     Area getMeepleTileArea(Tile tile, int size, Location d);
     Map<Location, Area> getMeepleTileAreas(Tile tile, int size, Set<Location> locations);

}
