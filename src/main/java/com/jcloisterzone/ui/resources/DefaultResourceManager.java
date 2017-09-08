package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.ui.ImmutablePoint;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class DefaultResourceManager implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final ImageLoader imgLoader;

    public DefaultResourceManager() {
        ImageLoader imgLoader = null;
        try {
            URL defaults = getClass().getClassLoader().getResource("defaults/").toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[] { defaults });
            imgLoader = new ImageLoader(loader);
        } catch (URISyntaxException | MalformedURLException e) {
            //should never happen
            logger.error(e.getMessage(), e);
        }
        this.imgLoader = imgLoader;
    }

    @Override
    public TileImage getTileImage(TileDefinition tile, Rotation rot) {
        return null;
    }

    @Override
    public TileImage getAbbeyImage(Rotation rot) {
        return null;
    }

    @Override
    public Image getImage(String path) {
        return imgLoader.getImage(path);
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
        return imgLoader.getLayeredImage(lid);
    }

    @Override
    public ImmutablePoint getBarnPlacement() {
        return ImmutablePoint.ZERO;
    }

    @Override
    public ImmutablePoint getMeeplePlacement(TileDefinition tile, Rotation rot, Location loc) {
        return null;
    }

    @Override
    public FeatureArea getBarnArea() {
        int rx = NORMALIZED_SIZE / 2;
        int ry = NORMALIZED_SIZE / 2;
        Area a = new Area(new Ellipse2D.Double(-rx, -ry, NORMALIZED_SIZE, NORMALIZED_SIZE));
        return (new FeatureArea(a, FeatureArea.DEFAULT_FARM_ZINDEX)).setFixed(true);
    }


    @Override
    public FeatureArea getBridgeArea(Location bridgeLoc) {
        return null;
    }

    @Override
    public FeatureArea getFeatureArea(TileDefinition tile, Rotation rotation, Location loc) {
//        if (loc.isCityOfCarcassonneQuarter()) {
//            double rx = NORMALIZED_SIZE * 0.6;
//            double ry = NORMALIZED_SIZE * 0.6;
//            ImmutablePoint offset = COUNT_OFFSETS.get(loc).get();
//            Area a = new Area(new Ellipse2D.Double(-rx+offset.getX(),-ry+offset.getY(),2*rx,2*ry));
//            return new FeatureArea(a, FeatureArea.DEFAULT_STRUCTURE_ZINDEX);
//        }
        return null;
    }
}
