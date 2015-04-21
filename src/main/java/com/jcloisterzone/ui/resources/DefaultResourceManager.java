package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

public class DefaultResourceManager implements ResourceManager {


    @Override
    public Image getTileImage(Tile tile) {
        return (new TileImageFactory()).getTileImage(tile);
    }

    @Override
    public Image getAbbeyImage() {
        return (new TileImageFactory()).getAbbeyImage();
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

    public Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
        Map<Location, Area> result = new HashMap<>();
        for (Location corner : corners) {
            int r = size/2;
            Area a = new Area(new Ellipse2D.Double(-r,-r,2*r,2*r));
            if (corner.isPartOf(Location.NR.union(Location.EL))) a.transform(Rotation.R90.getAffineTransform(size));
            if (corner.isPartOf(Location.SL.union(Location.ER))) a.transform(Rotation.R180.getAffineTransform(size));
            if (corner.isPartOf(Location.SR.union(Location.WL))) a.transform(Rotation.R270.getAffineTransform(size));
            result.put(corner, a);
        }
        return result;
    }


    @Override
    public Map<Location, Area> getBridgeAreas(Tile tile, int size, Set<Location> locations) {
        return null;
    }

    @Override
    public Map<Location, Area> getFeatureAreas(Tile tile, int size,
            Set<Location> locations) {
        return null;
    }



}
