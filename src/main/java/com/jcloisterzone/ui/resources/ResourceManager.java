package com.jcloisterzone.ui.resources;

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

     Image getTileImage(Tile tile);
     Image getAbbeyImage();

     //NOT used yet
     Area getFeatureArea(Tile tile, Feature piece, Location loc);


     //TODO use tile id instead?? but rotation must be also passed. API revision needed

     /** returns meeple offset on tile */
     ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Feature piece);

     Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners);

     //TODO add tile parameter !!! move default impl to default resource manager
     Area getBridgeArea(int size, Location loc);
     Map<Location, Area> getBridgeAreas(int size, Set<Location> locations);
     Area getMeepleTileArea(Tile tile, int size, Location d);
     Map<Location, Area> getMeepleTileAreas(Tile tile, int size, Set<Location> locations);

}
