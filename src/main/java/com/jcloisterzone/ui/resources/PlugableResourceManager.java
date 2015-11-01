package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
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
    	return getTileImage(tile, tile.getRotation());
    }

    @Override
    public Image getTileImage(Tile tile, Rotation rot) {
        for (ResourceManager manager : managers) {
            Image result = manager.getTileImage(tile, rot);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Image getAbbeyImage(Rotation rot) {
        for (ResourceManager manager : managers) {
            Image result = manager.getAbbeyImage(rot);
            if (result != null) return result;
        }
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
    public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int width, int height, Set<Location> corners) {
        for (ResourceManager manager : managers) {
            Map<Location, FeatureArea> result = manager.getBarnTileAreas(tile, width, height, corners);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int width, int height, Set<Location> locations) {
        for (ResourceManager manager : managers) {
            Map<Location, FeatureArea> result = manager.getBridgeAreas(tile, width, height, locations);
            if (result != null) return result;
        }
        return null;
    }


    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int width, int height, Set<Location> locations) {
        for (ResourceManager manager : managers) {
            Map<Location, FeatureArea> result = manager.getFeatureAreas(tile, width, height, locations);
            if (result != null) return result;
        }
        return null;
    }

}
