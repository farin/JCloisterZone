package com.jcloisterzone.ui.plugin;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.legacy.FigurePositionProvider;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.theme.AreaProvider;

public class ResourcePlugin extends Plugin implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final int NORMALIZED_SIZE = 1000;

    private AreaProvider areaProvider;
    private FigurePositionProvider figurePositionProvider; //legacy

    public ResourcePlugin(URL url) throws MalformedURLException {
        super(url);
        areaProvider = new AreaProvider(getLoader().getResource("tiles/display.xml"));
        figurePositionProvider = new FigurePositionProvider(areaProvider);
//        try {
//            addURLToSystemClassLoader(getUrl()); //use get URL to get sanitized URL
//        } catch (IntrospectionException e) {
//            logger.error(e.getMessage(), e);
//        }
    }

//    @Deprecated
//    public static void addURLToSystemClassLoader(URL url) throws IntrospectionException {
//        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//        Class<URLClassLoader> classLoaderClass = URLClassLoader.class;
//
//        try {
//            java.lang.reflect.Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
//            method.setAccessible(true);
//            method.invoke(systemClassLoader, new Object[] { url });
//        } catch (Throwable t) {
//            t.printStackTrace();
//            throw new IntrospectionException("Error when adding url to system ClassLoader ");
//        }
//    }

    protected Image getImageResource(String path) {
        return Toolkit.getDefaultToolkit().getImage(getLoader().getResource(path));
    }

    protected boolean containsTile(String tileId) {
        return true;
    }

    @Override
    public Image getTileImage(Tile tile) {
        return getTileImage(tile.getId());
    }

    @Override
    public Image getAbbeyImage() {
        return getTileImage(Tile.ABBEY_TILE_ID);
    }

    private Image getTileImage(String tileId) {
        //return null;
        if (!containsTile(tileId)) return null;
        String fileName = "tiles/"+tileId.substring(0, 2) + "/" + tileId.substring(3) + ".jpg";
        Image img = getImageResource(fileName);
        if (img == null) return null;
        return (new ImageIcon(img)).getImage();
    }


    @Override
    public Area getFeatureArea(Tile tile, Feature piece, Location loc) {
        if (!containsTile(tile.getId())) return null;
        return null;
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Feature piece) {
        if (!containsTile(tile.getId())) return null;
        if (type.equals(Barn.class)) return null;
        return figurePositionProvider.getFigurePlacement(tile, piece.getClass(), piece.getLocation());
    }

    public Area getMeepleTileArea(Tile tile, int size, Location d) {
        Map<Location, Area> areas = getMeepleTileAreas(tile, size, Collections.singleton(d));
        if (areas.isEmpty()) return null;
        return areas.values().iterator().next();
    }

    public Map<Location, Area> getMeepleTileAreas(Tile tile, int size, Set<Location> locations) {
        Map<Location, Area> areas = Maps.newHashMap();
        Area subsBridge = getBaseRoadAndCitySubstractions(tile);
        Area subsRoadCity = new Area(subsBridge);
        substractBridge(subsRoadCity, tile);
        Area subsFarm = getFarmSubstractions(tile);

        for(Feature piece : tile.getFeatures()) {
            Location loc = piece.getLocation();
            if (! locations.contains(loc)) {
                continue;
            }

            if (piece instanceof Farm) {
                areas.put(loc, getFarmArea(loc, tile, subsFarm));
                continue;
            }
            Area a = areaProvider.getArea(tile, piece, loc);
            if (piece instanceof City || piece instanceof Road) {
                Area subs = subsRoadCity;
                if (piece instanceof Bridge) {
                    subs = subsBridge;
                }
                if (! subs.isEmpty()) {
                    a = new Area(a); //copy to preserve original
                    a.subtract(subs);
                }
            }
            areas.put(loc, a);
        }
        Map<Location, Area> transformed = Maps.newHashMap();

        AffineTransform transform1;
        if (size == NORMALIZED_SIZE) {
            transform1 = new AffineTransform();
        } else {
            double ratio = size/(double)NORMALIZED_SIZE;
            transform1 = AffineTransform.getScaleInstance(ratio,ratio);
        }
        //TODO rotation - 3 rotations are done - Location rotation, getArea and this affine
        AffineTransform transform2 = tile.getRotation().getAffineTransform(size);

        for(Entry<Location, Area> entry : areas.entrySet()) {
            Area a = entry.getValue();
            a = a.createTransformedArea(transform1);
            a = a.createTransformedArea(transform2);
            transformed.put(entry.getKey(), a);
        }
        return transformed;
    }

    @Override
    public Map<Location, Area> getBarnTileAreas(Tile tile, int size, Set<Location> corners) {
        return null;
    }

    public Map<Location, Area> getBridgeAreas(int size, Set<Location> locations) {
        Map<Location, Area> result = Maps.newHashMap();
        for(Location loc : locations) {
            result.put(loc, getBridgeArea(size, loc));
        }
        return result;
    }

    //TODO move to Area Provider ???
    public Area getBridgeArea(int size, Location loc) {
        AffineTransform transform1;
        if (size == NORMALIZED_SIZE) {
            transform1 = new AffineTransform();
        } else {
            double ratio = size/(double)NORMALIZED_SIZE;
            transform1 = AffineTransform.getScaleInstance(ratio,ratio);
        }
        return areaProvider.getBridgeArea(loc).createTransformedArea(transform1);
    }

    private void substractBridge(Area substractions, Tile tile) {
        Bridge bridge = tile.getBridge();
        if (bridge != null) {
            Area area;
            area = areaProvider.getArea(tile, Bridge.class, bridge.getLocation());
            substractions.add(area);
        }
    }

    private Area getBaseRoadAndCitySubstractions(Tile tile) {
        Area sub = new Area();
        if (tile.getTower() != null) {
            sub.add(areaProvider.getArea(tile, Tower.class, Location.TOWER));
        }
        Area substraction = areaProvider.getSubstractionArea(tile);
        if (substraction != null) {
            sub.add(substraction);
        }
        return sub;
    }

    private Area getFarmSubstractions(Tile tile) {
        Area sub = new Area();
        for(Feature piece : tile.getFeatures()) {
            if (! (piece instanceof Farm)) {
                Area area = areaProvider.getArea(tile, piece.getClass(), piece.getLocation());
                sub.add(area);
            }
        }
        Area substraction = areaProvider.getSubstractionArea(tile);
        if (substraction != null) {
            sub.add(substraction);
        }
        return sub;
    }


    private Area getFarmArea(Location farm, Tile tile, Area sub) {
        Area base;
        if (areaProvider.isFarmComplement(tile, farm)) { //is complement farm
            base = new Area(new Rectangle(0,0, NORMALIZED_SIZE, NORMALIZED_SIZE));
            for(Feature piece : tile.getFeatures()) {
                if (piece instanceof Farm && ! piece.getLocation().isRotationOf(farm)) {
                    Area area = areaProvider.getArea(tile, Farm.class, piece.getLocation());
                    base.subtract(area);
                }
            }
        } else {
            base = areaProvider.getArea(tile, Farm.class, farm);
            base = new Area(base); //copy area to not substract from original
        }
        if (! sub.isEmpty()) {
            base.subtract(sub);
        }
        return base;
    }



}
