package com.jcloisterzone.ui.resources;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.plugin.Plugin;

/**
 * Delegates requests to child plugins
 */
public class PlugableResourceManager implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;
    private final List<ResourceManager> managers;


    public PlugableResourceManager(Client client, List<Plugin> plugins) {
        this.client = client;
        managers = new ArrayList<>();

        for (Plugin p: plugins) {
            if (p instanceof ResourceManager) {
                managers.add((ResourceManager) p);
            }
        }

        managers.add(new DefaultResourceManager());
    }

    @Override
    public Image getTileImage(Tile tile) {
        for (ResourceManager manager : managers) {
            Image result = manager.getTileImage(tile);
            if (result != null) return result;
        }
        logger.warn("Unable to load tile image for {}", tile.getId());
        return null;
    }

    @Override
    public Image getAbbeyImage() {
        for (ResourceManager manager : managers) {
            Image result = manager.getAbbeyImage();
            if (result != null) return result;
        }
        logger.warn("Unable to load tile Abbey image");
        return null;
    }

    @Override
    public Image getImage(String path) {
    	for (ResourceManager manager : managers) {
            Image result = manager.getImage(path);
            if (result != null) return result;
        }
    	logger.warn("Unable to load image {}", path);
        return null;
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
    	for (ResourceManager manager : managers) {
            Image result = manager.getLayeredImage(lid);
            if (result != null) return result;
        }
    	logger.warn("Unable to load layered image {}", lid.getBaseName());
        return null;
    }


    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        for (ResourceManager manager : managers) {
            ImmutablePoint result = manager.getMeeplePlacement(tile, type, loc);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
        for (ResourceManager manager : managers) {
            Map<Location, FeatureArea> result = manager.getBarnTileAreas(tile, size, corners);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int size, Set<Location> locations) {
        for (ResourceManager manager : managers) {
            Map<Location, FeatureArea> result = manager.getBridgeAreas(tile, size, locations);
            if (result != null) return result;
        }
        return null;
    }


    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int size, Set<Location> locations) {
        for (ResourceManager manager : managers) {
            Map<Location, FeatureArea> result = manager.getFeatureAreas(tile, size, locations);
            if (result != null) return result;
        }
        return null;
    }

}
