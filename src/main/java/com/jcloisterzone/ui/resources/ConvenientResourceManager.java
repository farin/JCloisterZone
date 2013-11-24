package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

/** extends resource manager with convenient methods
 * and add tile image caching
 */
public class ConvenientResourceManager implements ResourceManager {

    private final ResourceManager manager;
    private final Map<String, Image> imageCache = new HashMap<>();

    public ConvenientResourceManager(ResourceManager manager) {
        this.manager = manager;
    }

    //helper methods

    public Area getBridgeArea(Tile tile, int size, Location loc) {
        Map<Location, Area> result = manager.getBridgeAreas(tile, size, Collections.singleton(loc));
        return result.isEmpty() ? null : result.values().iterator().next();
    }

    public Area getMeepleTileArea(Tile tile, int size, Location loc) {
        Map<Location, Area> result =  manager.getFeatureAreas(tile, size, Collections.singleton(loc));
        return result.isEmpty() ? null : result.values().iterator().next();
    }

    //delegate methods

    @Override
    public Image getTileImage(Tile tile) {
        Image img = imageCache.get(tile.getId());
        if (img == null) {
            img = manager.getTileImage(tile);
            imageCache.put(tile.getId(), img);
        }
        return img;
    }

    @Override
    public Image getAbbeyImage() {
        Image img = imageCache.get(Tile.ABBEY_TILE_ID);
        if (img == null) {
            img = manager.getAbbeyImage();
            imageCache.put(Tile.ABBEY_TILE_ID, img);
        }
        return img;
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        return manager.getMeeplePlacement(tile, type, loc);
    }

    @Override
    public Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
        return manager.getBarnTileAreas(tile, size, corners);
    }

    @Override
    public Map<Location, Area> getBridgeAreas(Tile tile, int size, Set<Location> locations) {
        return manager.getBridgeAreas(tile, size, locations);
    }

    @Override
    public Map<Location, Area> getFeatureAreas(Tile tile, int size, Set<Location> locations) {
        return manager.getFeatureAreas(tile, size, locations);
    }

}
