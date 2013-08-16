package com.jcloisterzone.ui.theme;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

    //TODO descriptor type!
    private final Map<FeatureDescriptor, Area> areas = Maps.newHashMap();
    private final Map<String, Area> substraction = Maps.newHashMap(); //key tile ID
    private final Set<FeatureDescriptor> complementFarms = Sets.newHashSet();
    private final Map<FeatureDescriptor, ImmutablePoint> points;

    static private final Map<FeatureDescriptor, ImmutablePoint> defaultPoints;

    private static final Area BRIDGE_AREA_NS, BRIDGE_AREA_WE;

    static {
        Area a = new Area(new Rectangle(400, 0, 200, 1000));
        a.subtract(new Area(new Ellipse2D.Double(300, 150, 200, 700)));
        BRIDGE_AREA_NS = new Area(a);
        a.transform(Rotation.R270.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
        BRIDGE_AREA_WE = a;

        defaultPoints = (new PointsParser(ThemeGeometry.class.getClassLoader().getResource("defaults/points.xml"))).parse();
    }

    public ThemeGeometry(ClassLoader loader) throws IOException, SAXException, ParserConfigurationException {
        Element shapes = XmlUtils.parseDocument(loader.getResource("tiles/shapes.xml")).getDocumentElement();

        NodeList nl = shapes.getElementsByTagName("shape");
        for (int i = 0; i < nl.getLength(); i++) {
            processShapeElement((Element) nl.item(i));
        }
        nl = shapes.getElementsByTagName("complement-farm");
        for (int i = 0; i < nl.getLength(); i++) {
            processComplementFarm((Element) nl.item(i));
        }

        points = (new PointsParser(loader.getResource("tiles/points.xml"))).parse();
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
                assert !areas.containsKey(fd);
                areas.put(fd, area.createTransformedArea(transform));
            }

            @Override
            public void processSubstract(Element node, String tileId, AffineTransform transform) {
                //TODO merge if already exists
                assert !substraction.containsKey(tileId);
                substraction.put(tileId, area.createTransformedArea(transform));
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

    public Area getArea(Tile tile, Feature feature, Location loc) {
        return getArea(tile, feature.getClass(), loc);
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
        FeatureDescriptor descriptor = new FeatureDescriptor(tile.getId(), featureClass, loc);
        Area area = areas.get(descriptor);
        if (area == null) {
            //try generic descriptor
            FeatureDescriptor genericDescriptor = new FeatureDescriptor(FeatureDescriptor.EVERY, featureClass, loc);
            area = areas.get(genericDescriptor);
            if (area == null) {
                logger.error("No shape defined for <" + descriptor + ">");
                area = new Area(); //return empty Area
            }
        }
        return area;
    }

    public Area getBridgeArea(Location loc) {
        //TODO use shapes.xml to define areas ? (but it is too complicated shape)
        if (loc == Location.NS) return BRIDGE_AREA_NS;
        if (loc == Location.WE) return BRIDGE_AREA_WE;
        throw new IllegalArgumentException("Incorrect location");
    }

    public Area getSubstractionArea(Tile tile) {
        return substraction.get(tile.getId());
    }

    public boolean isFarmComplement(Tile tile, Location loc) {
        loc = loc.rotateCCW(tile.getRotation());
        FeatureDescriptor fd = new FeatureDescriptor(tile.getId(), Farm.class, loc);
        if (complementFarms.contains(fd)) return true;
        fd = new FeatureDescriptor(FeatureDescriptor.EVERY, Farm.class, loc);
        if (complementFarms.contains(fd)) return true;
        return false;
    }

    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Feature> feature, Location location) {
        Location normalizedLoc = location.rotateCCW(tile.getRotation());
        FeatureDescriptor fd = new FeatureDescriptor(tile.getId(), feature, normalizedLoc);
        ImmutablePoint point = getMeeplePlacement(points, fd);
        if (point == null) {
            point = getMeeplePlacement(defaultPoints, fd);
            if (point == null) {
                logger.warn("No point defined for <" + fd + ">");
                point =  new ImmutablePoint(0, 0);
            }
        }
        //System.out.println(fd + " | " + point + " | " + point.rotate(tile.getRotation()) + " | " + tile.getRotation());
        return point.rotate(tile.getRotation());
    }

    private ImmutablePoint getMeeplePlacement(Map<FeatureDescriptor, ImmutablePoint> source, FeatureDescriptor fd) {
        ImmutablePoint point = source.get(fd);
        if (point == null) {
            fd = new FeatureDescriptor(FeatureDescriptor.EVERY, fd.getFeatureType(), fd.getLocation());
            point = source.get(fd);
        }
        return point;
    }
}