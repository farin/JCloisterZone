package com.jcloisterzone.ui.resources;

import java.awt.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.plugin.MergedAliases;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.plugin.ResourcePlugin;
import com.jcloisterzone.ui.ImmutablePoint;

import io.vavr.Predicates;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

/**
 * Delegates requests to child plugins
 */
public class PlugableResourceManager implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final Vector<ResourceManager> managers;

    public PlugableResourceManager(Iterable<Plugin> plugins) {
        managers = Stream.ofAll(plugins)
            .filter(p -> p.isEnabled())
            .filter(Predicates.instanceOf(ResourceManager.class))
            .map(p -> (ResourceManager) p)
            .append(new DefaultResourceManager())
            .toVector();

        MergedAliases mergedAliases = new MergedAliases(plugins);
        managers.forEach(rm -> {
            if (rm instanceof ResourcePlugin) {
                ((ResourcePlugin)rm).setMergedAliases(mergedAliases);
            }
        });
    }

    @Override
    public TileImage getTileImage(String tileId, Rotation rot) {
        for (ResourceManager manager : managers) {
            TileImage result = manager.getTileImage(tileId, rot);
            if (result != null) return result;
        }
        logger.warn("Unable to load tile image for {}", tileId);
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
    public ImmutablePoint getMeeplePlacement(Tile tile, Rotation rot, Location loc) {
        for (ResourceManager manager : managers) {
            ImmutablePoint result = manager.getMeeplePlacement(tile, rot, loc);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public ImmutablePoint getBarnPlacement() {
        for (ResourceManager manager : managers) {
            ImmutablePoint result = manager.getBarnPlacement();
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public FeatureArea getBarnArea() {
        for (ResourceManager manager : managers) {
            FeatureArea result = manager.getBarnArea();
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public FeatureArea getBridgeArea(Location bridgeLoc) {
        for (ResourceManager manager : managers) {
            FeatureArea result = manager.getBridgeArea(bridgeLoc);
            if (result != null) return result;
        }
        return null;
    }


    @Override
    public FeatureArea getFeatureArea(Tile tile, Rotation rot, Location loc) {
        for (ResourceManager manager : managers) {
            FeatureArea result = manager.getFeatureArea(tile, rot, loc);
            if (result != null) return result;
        }
        return null;
    }

}
