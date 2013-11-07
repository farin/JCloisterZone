package com.jcloisterzone.ui.theme;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.plugin.ResourcePlugin;
import com.jcloisterzone.ui.theme.SvgTransformationCollector.GeometryHandler;


public class ThemeGeometry {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> aliases = new HashMap<>();
    private final Map<FeatureDescriptor, Area> areas = new HashMap<>();
    private final Map<String, Area> substractionAll = new HashMap<>(); //key tile ID
    private final Map<String, Area> substractionFarm = new HashMap<>(); //key tile ID
    private final Set<FeatureDescriptor> complementFarms = new HashSet<>();
    private final Map<FeatureDescriptor, ImmutablePoint> points;

    private static final Area BRIDGE_AREA_NS, BRIDGE_AREA_WE;

    static {
        Area a = new Area(new Rectangle(400, 0, 200, 1000));
        a.subtract(new Area(new Ellipse2D.Double(300, 150, 200, 700)));
        BRIDGE_AREA_NS = new Area(a);
        a.transform(Rotation.R270.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
        BRIDGE_AREA_WE = a;
    }

    public ThemeGeometry(ClassLoader loader, String folder) throws IOException, SAXException, ParserConfigurationException {
        NodeList nl;
        URL aliasesResource = loader.getResource(folder + "/aliases.xml");
        if (aliasesResource != null) {
            Element aliasesEl = XmlUtils.parseDocument(aliasesResource).getDocumentElement();
            nl = aliasesEl.getElementsByTagName("alias");
            for (int i = 0; i < nl.getLength(); i++) {
                Element alias = (Element) nl.item(i);
                aliases.put(alias.getAttribute("treat"), alias.getAttribute("as"));
            }
        }

        Element shapes = XmlUtils.parseDocument(loader.getResource(folder +"/shapes.xml")).getDocumentElement();
        nl = shapes.getElementsByTagName("shape");
        for (int i = 0; i < nl.getLength(); i++) {
            processShapeElement((Element) nl.item(i));
        }
        nl = shapes.getElementsByTagName("complement-farm");
        for (int i = 0; i < nl.getLength(); i++) {
            processComplementFarm((Element) nl.item(i));
        }

        points = (new PointsParser(loader.getResource(folder + "/points.xml"))).parse();
    }

    private FeatureDescriptor createFeatureDescriptor(String featureName, String tileAndLocation) {
        String[] tokens = tileAndLocation.split(" ");
        return FeatureDescriptor.valueOf(tokens[0], featureName, tokens[1]);
    }

    private Area createArea(Element shapeNode) {
        NodeList nl = shapeNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element el = (Element) nl.item(i);
                if (el.getNodeName().startsWith("svg:")) {
                    return new SvgToShapeConverter().convert(el);
                }
            }
        }
        throw new IllegalArgumentException("Node doesn't contains svg shape.");
    }

    private void processShapeElement(Element shapeNode) {
        final Area area = createArea(shapeNode);

        SvgTransformationCollector transformCollector = new SvgTransformationCollector(shapeNode);
        transformCollector.collect(new GeometryHandler() {

            @Override
            public void processApply(Element node, FeatureDescriptor fd, AffineTransform transform) {
                assert !areas.containsKey(fd) : "Duplicate key " + fd;
                areas.put(fd, area.createTransformedArea(transform));
            }

            @Override
            public void processSubstract(Element node, String tileId, AffineTransform transform, boolean isFarm) {
                Map<String, Area> target = isFarm ? substractionFarm : substractionAll;
                //TODO merge if already exists
                assert !target.containsKey(tileId);
                target.put(tileId, area.createTransformedArea(transform));
            }

        });
    }

    private void processComplementFarm(Element xml) {
        NodeList nl = xml.getElementsByTagName("apply");
        for (int i = 0; i < nl.getLength(); i++) {
            Element apply = (Element) nl.item(i);
            FeatureDescriptor fd = createFeatureDescriptor("FARM", apply.getTextContent());
            complementFarms.add(fd);
        }
    }

    private FeatureDescriptor[] getLookups(Tile tile, Class<? extends Feature> featureType, Location location) {
        String alias = aliases.get(tile.getId());
        FeatureDescriptor[] fd = new FeatureDescriptor[alias == null ? 2 : 3];
        fd[0] = new FeatureDescriptor(tile.getId(), featureType, location);
        if (alias != null) {
            fd[1] = new FeatureDescriptor(alias, featureType, location);
        }
        fd[alias == null ? 1 : 2] = new FeatureDescriptor(FeatureDescriptor.EVERY, featureType, location);
        return fd;
    }

    public Area getArea(Tile tile, Class<? extends Feature> featureClass, Location loc) {
        Rotation tileRotation = tile.getRotation();
        if (featureClass.equals(Bridge.class)) {
            Area a =  getBridgeArea(loc.rotateCCW(tileRotation));
            //bridge is independent on tile rotation
            if ((loc == Location.WE && (tileRotation == Rotation.R90 || tileRotation == Rotation.R180)) ||
                (loc == Location.NS && (tileRotation == Rotation.R180 || tileRotation == Rotation.R270))) {
                a = new Area(a);
                a.transform(Rotation.R180.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
            }
            return a;
        }
        loc = loc.rotateCCW(tileRotation);
        FeatureDescriptor lookups[] = getLookups(tile, featureClass, loc);
        Area area;
        for (FeatureDescriptor fd : lookups) {
            area = areas.get(fd);
            if (area != null) return area;
        }
        return null;
    }

    public Area getBridgeArea(Location loc) {
        //TODO use shapes.xml to define areas ? (but it is too complicated shape)
        if (loc == Location.NS) return BRIDGE_AREA_NS;
        if (loc == Location.WE) return BRIDGE_AREA_WE;
        throw new IllegalArgumentException("Incorrect location");
    }

    public Area getSubstractionArea(Tile tile, boolean isFarm) {
        if (isFarm) {
            Area area = getSubstractionArea(substractionFarm, tile);
            if (area != null) return area;
        }
        return getSubstractionArea(substractionAll, tile);
    }

    private Area getSubstractionArea(Map<String, Area> substractions, Tile tile) {
        Area area = substractions.get(tile.getId());
        if (area == null) {
            String alias = aliases.get(tile.getId());
            if (alias != null) {
                area = substractions.get(alias);
            }
        }
        return area;
    }

    public boolean isFarmComplement(Tile tile, Location loc) {
        loc = loc.rotateCCW(tile.getRotation());
        FeatureDescriptor lookups[] = getLookups(tile, Farm.class, loc);
        for (FeatureDescriptor fd : lookups) {
            if (complementFarms.contains(fd)) return true;
        }
        return false;
    }

    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Feature> feature, Location location) {
        Location normalizedLoc = location.rotateCCW(tile.getRotation());
        FeatureDescriptor lookups[] = getLookups(tile, feature, normalizedLoc);
        ImmutablePoint point = null;
        for (FeatureDescriptor fd : lookups) {
            point = points.get(fd);
            if (point != null) break;
        }
        if (point == null) return null;
        return point.rotate(tile.getRotation());
    }
}