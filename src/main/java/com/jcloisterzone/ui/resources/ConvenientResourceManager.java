package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.util.WeakHashMap;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.ImmutablePoint;

// move caching on PluggableResourceManager ? or rename to cached manager
public class ConvenientResourceManager implements ResourceManager {

    private final ResourceManager manager;
    private final WeakHashMap<String, Object> imageCache = new WeakHashMap<>(64);


    public ConvenientResourceManager(ResourceManager manager) {
        this.manager = manager;
    }

    public void clearCache() {
        imageCache.clear();
    }

    //delegate methods

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
    public ImmutablePoint getMeeplePlacement(Tile tile, Rotation rot, Location loc) {
        return manager.getMeeplePlacement(tile, rot, loc);
    }

    @Override
    public ImmutablePoint getBarnPlacement() {
        return manager.getBarnPlacement();
    }

    @Override
    public FeatureArea getBarnArea() {
        return manager.getBarnArea();
    }

    @Override
    public FeatureArea getBridgeArea(Location bridgeLoc) {
        return manager.getBridgeArea(bridgeLoc);
    }

    @Override
    public FeatureArea getFeatureArea(Tile tile, Rotation rot, Location loc) {
        return manager.getFeatureArea(tile, rot, loc);
    }

}
