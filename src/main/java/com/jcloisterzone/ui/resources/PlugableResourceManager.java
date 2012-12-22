package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
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
        managers = Lists.newArrayList();

        for (Plugin p: plugins) {
            if (p instanceof ResourceManager) {
                managers.add((ResourceManager) p);
            }
        }

        managers.add(new DefaultResourceManager());
    }

    @Override
    public Image getTileImage(String tileId) {
        for (ResourceManager manager : managers) {
            Image result = manager.getTileImage(tileId);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Area getFeatureArea(String tileId, Feature piece, Location loc) {
        for (ResourceManager manager : managers) {
            Area result = manager.getFeatureArea(tileId, piece, loc);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Feature piece) {
        for (ResourceManager manager : managers) {
            ImmutablePoint result = manager.getMeeplePlacement(tile, type, piece);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
        for (ResourceManager manager : managers) {
            Map<Location, Area> result = manager.getBarnTileAreas(tile, size, corners);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Area getBridgeArea(int size, Location loc) {
        for (ResourceManager manager : managers) {
            Area result = manager.getBridgeArea(size, loc);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Map<Location, Area> getBridgeAreas(int size, Set<Location> locations) {
        for (ResourceManager manager : managers) {
            Map<Location, Area> result = manager.getBridgeAreas(size, locations);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Area getMeepleTileArea(Tile tile, int size, Location d) {
        for (ResourceManager manager : managers) {
            Area result = manager.getMeepleTileArea(tile, size, d);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Map<Location, Area> getMeepleTileAreas(Tile tile, int size, Set<Location> locations) {
        for (ResourceManager manager : managers) {
            Map<Location, Area> result = manager.getMeepleTileAreas(tile, size, locations);
            if (result != null) return result;
        }
        return null;
    }

}
