package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
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

    public void clearCache() {
        imageCache.clear();
    }

    //helper methods

    public FeatureArea getBridgeArea(Tile tile, int width, int height, Location loc) {
        Map<Location, FeatureArea> result = manager.getBridgeAreas(tile, width, height, Collections.singleton(loc));
        return result.isEmpty() ? null : result.values().iterator().next();
    }

    public FeatureArea getMeepleTileArea(Tile tile, int width, int height, Location loc) {
        Map<Location, FeatureArea> result =  manager.getFeatureAreas(tile, width, height, Collections.singleton(loc));
        return result.isEmpty() ? null : result.values().iterator().next();
    }

    //delegate methods

    @Override
    public Image getTileImage(Tile tile) {
    	return getTileImage(tile, tile.getRotation());
    }

    @Override
    public Image getTileImage(Tile tile, Rotation rot) {
    	String key = tile.getId()+"@"+rot.toString();
        Image img = imageCache.get(key);
        if (img == null) {
            img = manager.getTileImage(tile, rot);
            imageCache.put(key, img);
        }
        return img;
    }

    @Override
    public Image getAbbeyImage(Rotation rot) {
    	String key = Tile.ABBEY_TILE_ID+"@"+rot.toString();
        Image img = imageCache.get(key);
        if (img == null) {
            img = manager.getAbbeyImage(rot);
            imageCache.put(key, img);
        }
        return img;
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        return manager.getMeeplePlacement(tile, type, loc);
    }

    @Override
    public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int width, int height, Set<Location> corners) {
        return manager.getBarnTileAreas(tile, width, height, corners);
    }

    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int width, int height, Set<Location> locations) {
        return manager.getBridgeAreas(tile, width, height, locations);
    }

    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int width, int height, Set<Location> locations) {
        return manager.getFeatureAreas(tile, width, height, locations);
    }

}
