package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
    private final Map<String, Object> imageCache = new WeakHashMap<>(64);


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
    public TileImage getTileImage(Tile tile) {
        return getTileImage(tile, tile.getRotation());
    }

    @Override
    public TileImage getTileImage(Tile tile, Rotation rot) {
        String key = tile.getId()+"@"+rot.toString();
        TileImage img = (TileImage) imageCache.get(key);
        if (img == null) {
            img = manager.getTileImage(tile, rot);
            imageCache.put(key, img);
        }
        return img;
    }

    @Override
    public TileImage getAbbeyImage(Rotation rot) {
        String key = Tile.ABBEY_TILE_ID+"@"+rot.toString();
        TileImage img = (TileImage) imageCache.get(key);
        if (img == null) {
            img = manager.getAbbeyImage(rot);
            imageCache.put(key, img);
        }
        return img;
    }

    @Override
    public Image getImage(String path) {
    	Image img = (Image) imageCache.get(path);
    	if (img == null) {
    		img = manager.getImage(path);
    		imageCache.put(path, img);
    	}
    	return img;
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
    	String key = lid.toString();
        Image img = (Image) imageCache.get(key);
        if (img == null) {
        	img = manager.getLayeredImage(lid);
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
