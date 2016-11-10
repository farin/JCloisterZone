package com.jcloisterzone.ui.plugin;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.FeatureDescriptor;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.ui.resources.svg.ThemeGeometry;


public class ResourcePlugin extends Plugin implements ResourceManager {

    public static final int NORMALIZED_SIZE = 1000;

    private static ThemeGeometry defaultGeometry;
    private ThemeGeometry pluginGeometry;
    private Insets imageOffset =  new Insets(0, 0, 0, 0);
    private int imageRatioX = 1;
    private int imageRatioY = 1;

    private Set<String> supportedExpansions = new HashSet<>(); //expansion codes

    static {
        try {
            defaultGeometry = new ThemeGeometry(ResourcePlugin.class.getClassLoader(), "defaults/tiles", 1.0);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LoggerFactory.getLogger(ThemeGeometry.class).error(e.getMessage(), e);
        }
    }

    public ResourcePlugin(URL url, String relativePath) throws Exception {
        super(url, relativePath);
    }

    @Override
    protected void doLoad() throws IOException, SAXException, ParserConfigurationException {
        pluginGeometry = new ThemeGeometry(getLoader(), "tiles", getImageSizeRatio());
    }

    @Override
    protected void parseMetadata(Element rootElement) throws Exception {
        super.parseMetadata(rootElement);
        NodeList nl = rootElement.getElementsByTagName("expansions");
        if (nl.getLength() == 0) throw new Exception("Supported expansions missing in plugin.xml for " + getId());
        Element expansion = (Element) nl.item(0);
        nl = expansion.getElementsByTagName("expansion");
        if (nl.getLength() == 0) throw new Exception("No expansion is supported by " + getId());
        for (int i = 0; i < nl.getLength(); i++) {
            String expName = nl.item(i).getFirstChild().getNodeValue().trim();
            Expansion exp = Expansion.valueOf(expName);
            supportedExpansions.add(exp.getCode());
        }

        Element tiles = XMLUtils.getElementByTagName(rootElement, "tiles");
        if (tiles != null) {
            String value = XMLUtils.childValue(tiles, "image-offset");
            if (value != null) {
                String[] tokens = value.split(",");
                if (tokens.length != 4) {
                    throw new Exception("Invalid value for image-offset " + value);
                }
                imageOffset = new Insets(
                   Integer.parseInt(tokens[0]),
                   Integer.parseInt(tokens[1]),
                   Integer.parseInt(tokens[2]),
                   Integer.parseInt(tokens[3])
                );
            }
            value = XMLUtils.childValue(tiles, "image-ratio-x");
            if (value != null) {
                imageRatioX = Integer.parseInt(value);
				if (imageRatioX == 0)
					imageRatioX = 1;
            }
            value = XMLUtils.childValue(tiles, "image-ratio-y");
            if (value != null) {
                imageRatioY = Integer.parseInt(value);
                if (imageRatioY == 0) imageRatioY = 1;
            }
        }
    }


    protected boolean containsTile(String tileId) {
        if (!isEnabled()) return false;
        String expCode = tileId.substring(0, 2);
        return supportedExpansions.contains(expCode);
    }

    public boolean isExpansionSupported(Expansion exp) {
        return supportedExpansions.contains(exp.getCode());
    }

    public double getImageSizeRatio() {
        return imageRatioY/(double)imageRatioX;
    }

    @Override
    public TileImage getTileImage(Tile tile) {
        return getTileImage(tile.getId(), tile.getRotation());
    }

    @Override
    public TileImage getTileImage(Tile tile, Rotation rot) {
        return getTileImage(tile.getId(), rot);
    }

    @Override
    public TileImage getAbbeyImage(Rotation rot) {
        return getTileImage(Tile.ABBEY_TILE_ID, rot);
    }

    private TileImage getTileImage(String tileId, Rotation rot) {
        if (!containsTile(tileId)) return null;
        String baseName = "tiles/"+tileId.substring(0, 2) + "/" + tileId.substring(3);
        String fileName;
        Image img;
        // first try to find rotation specific image
        fileName = baseName + "@" + rot.ordinal();
        img =  getImageLoader().getImage(fileName);
        if (img != null) {
            return new TileImage(img, imageOffset);
        }
        // if not found, load generic one and rotate manually
        fileName = baseName;
        img =  getImageLoader().getImage(fileName);
        if (img == null) return null;
        if (rot == Rotation.R0) {
            return new TileImage(img, imageOffset);
        }
        BufferedImage buf;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (rot == Rotation.R180) {
            buf = UiUtils.newTransparentImage(w, h);
        } else {
            buf = UiUtils.newTransparentImage(h, w);
        }
        Graphics2D g = (Graphics2D) buf.getGraphics();
        g.drawImage(img, rot.getAffineTransform(w, h), null);
        return new TileImage(buf, imageOffset);
    }

    @Override
    public Image getImage(String path) {
        return getImageLoader().getImage(path);
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
        return getImageLoader().getLayeredImage(lid);
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        if (!containsTile(tile.getId())) return null;
        if (type.equals(Barn.class)) return null;
        Feature piece = tile.getFeature(loc);
        ImmutablePoint point = pluginGeometry.getMeeplePlacement(tile, piece.getClass(), loc);
        if (point == null) {
            point = defaultGeometry.getMeeplePlacement(tile, piece.getClass(), piece.getLocation());
        }
        if (point == null) {
            logger.warn("No point defined for <" + (new FeatureDescriptor(tile, piece.getClass(), loc)) + ">");
            point =  new ImmutablePoint(0, 0);
        }
        return point;
    }

    private FeatureArea getFeatureArea(Tile tile, Class<? extends Feature> featureClass, Location loc) {
        if (loc == Location.ABBOT) loc = Location.CLOISTER;
        if (Castle.class.equals(featureClass)) {
            featureClass = City.class;
        }
        FeatureArea area = pluginGeometry.getArea(tile, featureClass, loc);
        if (area == null) {
            area = adaptDefaultGeometry(defaultGeometry.getArea(tile, featureClass, loc));
        }
        if (area == null) {
            logger.error("No shape defined for <" + (new FeatureDescriptor(tile, featureClass, loc)) + ">");
            area = new FeatureArea(new Area(), 0);
        }
        return area;
    }

    private Area getSubstractionArea(Tile tile, boolean farm) {
        Area d = defaultGeometry.getSubstractionArea(tile, farm),
             p = pluginGeometry.getSubstractionArea(tile, farm),
             area = new Area();

        if (d != null) area.add(adaptDefaultGeometry(d));
        if (p != null) area.add(p);
        return area;
    }

    private boolean isFarmComplement(Tile tile, Location loc) {
        if (pluginGeometry.isFarmComplement(tile, loc)) return true;
        if (defaultGeometry.isFarmComplement(tile, loc)) return true;
        return false;
    }
    
    private FeatureArea adaptDefaultGeometry(FeatureArea fa) {
    	if (fa == null) return null;
    	return new FeatureArea(
    		adaptDefaultGeometry(fa.getTrackingArea()),
    		adaptDefaultGeometry(fa.getDisplayArea()),
    		fa.getzIndex()
    	);    		
    }
    
    private Area adaptDefaultGeometry(Area a) {
    	if (a == null) return null;
    	if (imageRatioX != imageRatioY) {
    		return a.createTransformedArea(
    			AffineTransform.getScaleInstance(1.0, getImageSizeRatio())
    		);
    	} 
    	return a;
    }

    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int width, int height, Set<Location> locations) {
        if (!containsTile(tile.getId())) return null;

        Map<Location, FeatureArea> areas = new HashMap<>();
        Area subsBridge = getBaseRoadAndCitySubstractions(tile);
        Area subsRoadCity = new Area(subsBridge);
        substractBridge(subsRoadCity, tile);
        Area subsFarm = getFarmSubstractions(tile);

        for (Feature piece : tile.getFeatures()) {
            boolean aliasAbbot = false;
            Location loc = piece.getLocation();
            if (loc == Location.CLOISTER && locations.contains(Location.ABBOT)) {
                aliasAbbot = true;
            }
            if (!aliasAbbot && !locations.contains(loc)) {
                continue;
            }

            FeatureArea fa;
            if (piece instanceof Farm) {
                fa = getFarmArea(loc, tile, subsFarm);
                areas.put(loc, fa);
                continue;
            }

            fa = new FeatureArea(getFeatureArea(tile, piece.getClass(), loc)); //copy to preserve original
            if (piece instanceof City || piece instanceof Road) {
                Area subs = piece instanceof Bridge ? subsBridge : subsRoadCity;
                if (!subs.isEmpty()) {
                    fa.getTrackingArea().subtract(subs);
                }
            }
            loc =  aliasAbbot ? Location.ABBOT : loc;
            areas.put(loc, fa);
        }
        if (locations.contains(Location.FLIER)) {
            FeatureArea fa = new FeatureArea(getFeatureArea(tile, null, Location.FLIER));
            areas.put(Location.FLIER, fa);
        }

        AffineTransform transform1;
        if (width == NORMALIZED_SIZE && height == NORMALIZED_SIZE) {
            transform1 = new AffineTransform();
        } else {
            double ratioX = width / (double)NORMALIZED_SIZE;
            double ratioY = height / (double)NORMALIZED_SIZE / getImageSizeRatio();
            transform1 = AffineTransform.getScaleInstance(ratioX, ratioY);
        }
        //TODO rotation - 3 rotations are done - Location rotation, getArea and this affine
        AffineTransform transform2 = tile.getRotation().getAffineTransform(width, height);

        for (FeatureArea fa : areas.values()) {
            Area a = fa.getTrackingArea();
            a = a.createTransformedArea(transform1);
            a = a.createTransformedArea(transform2);
            fa.setTrackingArea(a);
        }

       return areas;
    }

    @Override
    public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int width, int height, Set<Location> corners) {
        return null;
    }

    //TODO Move to default provider ???
    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int width, int height, Set<Location> locations) {
        if (!isEnabled()) return null;
        Map<Location, FeatureArea> result = new HashMap<>();
        for (Location loc : locations) {
            result.put(loc, getBridgeArea(width, height, loc));
        }
        return result;
    }

    //TODO move to Area Provider ???
    private FeatureArea getBridgeArea(int width, int height, Location loc) {
        AffineTransform transform1;
        if (width == NORMALIZED_SIZE && height == NORMALIZED_SIZE) {
            transform1 = new AffineTransform();
        } else {
            double ratioX = width / (double)NORMALIZED_SIZE;
            double ratioY = height / (double)NORMALIZED_SIZE / getImageSizeRatio();
            transform1 = AffineTransform.getScaleInstance(ratioX,ratioY);
        }
        Area a = pluginGeometry.getBridgeArea(loc).createTransformedArea(transform1);
        return new FeatureArea(a, FeatureArea.DEFAULT_BRIDGE_ZINDEX);
    }

    private void substractBridge(Area substractions, Tile tile) {
        Bridge bridge = tile.getBridge();
        if (bridge != null) {
            Area area;
            area = getFeatureArea(tile, Bridge.class, bridge.getLocation()).getTrackingArea();
            substractions.add(area);
        }
    }

    private Area getBaseRoadAndCitySubstractions(Tile tile) {
        Area sub = new Area();
        if (tile.getTower() != null) {
            sub.add(getFeatureArea(tile, Tower.class, Location.TOWER).getTrackingArea());
        }
        if (tile.getFlier() != null) {
            sub.add(getFeatureArea(tile, null, Location.FLIER).getTrackingArea());
        }
        sub.add(getSubstractionArea(tile, false));
        return sub;
    }

    private Area getFarmSubstractions(Tile tile) {
        Area sub = new Area();
        for (Feature piece : tile.getFeatures()) {
            if (!(piece instanceof Farm)) {
                Area area = getFeatureArea(tile, piece.getClass(), piece.getLocation()).getTrackingArea();
                sub.add(area);
            }
        }
        if (tile.getFlier() != null) {
            sub.add(getFeatureArea(tile, null, Location.FLIER).getTrackingArea());
        }
        sub.add(getSubstractionArea(tile, true));
        return sub;
    }


    private FeatureArea getFarmArea(Location farm, Tile tile, Area sub) {
        FeatureArea result;
        if (isFarmComplement(tile, farm)) { //is complement farm
            Area base = new Area(new Rectangle(0,0, NORMALIZED_SIZE-1, (int) (NORMALIZED_SIZE * getImageSizeRatio()) - 1));
            for (Feature piece : tile.getFeatures()) {
                if (piece instanceof Farm && piece.getLocation() != farm) {
                    Area area = getFeatureArea(tile, Farm.class, piece.getLocation()).getTrackingArea();
                    base.subtract(area);
                }
            }
            result = new FeatureArea(base, FeatureArea.DEFAULT_FARM_ZINDEX);
        } else {
            //copy area to not substract from original
            result = new FeatureArea(getFeatureArea(tile, Farm.class, farm));
        }
        if (!sub.isEmpty()) {
            result.getTrackingArea().subtract(sub);
        }
        return result;
    }
}
