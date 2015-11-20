package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

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
    public Image getTileImage(Tile tile) {
        return (new TileImageFactory()).getTileImage(tile);
    }

    @Override
    public Image getAbbeyImage() {
        return (new TileImageFactory()).getAbbeyImage();
    }

    @Override
    public Image getImage(String path) {
    	return imgLoader.getImage(path);
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
    	return imgLoader.getLayeredImage(lid);
    }

    private ImmutablePoint getBarnPlacement(Location loc) {
        if (loc.intersect(Location.NL.union(Location.WR)) != null) return new ImmutablePoint(0, 0);
        if (loc.intersect(Location.NR.union(Location.EL)) != null) return new ImmutablePoint(100, 0);
        if (loc.intersect(Location.SL.union(Location.ER)) != null) return new ImmutablePoint(100, 100);
        if (loc.intersect(Location.SR.union(Location.WL)) != null) return new ImmutablePoint(0, 100);
        throw new IllegalArgumentException("Corner location expected");
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        if (type.equals(Barn.class)) {
            return getBarnPlacement(loc);
        }
        return null;
    }

    @Override
	public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
        Map<Location, FeatureArea> result = new HashMap<>();
        for (Location corner : corners) {
            int r = size/2;
            Area a = new Area(new Ellipse2D.Double(-r,-r,2*r,2*r));
            if (corner.isPartOf(Location.NR.union(Location.EL))) a.transform(Rotation.R90.getAffineTransform(size));
            if (corner.isPartOf(Location.SL.union(Location.ER))) a.transform(Rotation.R180.getAffineTransform(size));
            if (corner.isPartOf(Location.SR.union(Location.WL))) a.transform(Rotation.R270.getAffineTransform(size));
            result.put(corner, new FeatureArea(a, FeatureArea.DEFAULT_FARM_ZINDEX));
        }
        return result;
    }


    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int size, Set<Location> locations) {
        return null;
    }

    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int size, Set<Location> locations) {
        return null;
    }



}
