package com.jcloisterzone.ui.resources.svg;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
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

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.resources.AreaRotationScaling;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.FeatureDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.svg.SvgTransformationCollector.GeometryHandler;


public class ThemeGeometry {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static ThemeGeometry DEFAULT_GEOMETRY;
    private static final Area BRIDGE_AREA_NS, BRIDGE_AREA_WE;

    static {
        try {
            DEFAULT_GEOMETRY = new ThemeGeometry(Plugin.class.getClassLoader(), "defaults/tiles", 1.0);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LoggerFactory.getLogger(ThemeGeometry.class).error(e.getMessage(), e);
        }

        Area a = new Area(new Rectangle(400, 0, 200, 1000));
        a.subtract(new Area(new Ellipse2D.Double(300, 150, 200, 700)));
        BRIDGE_AREA_NS = new Area(a);
        a.transform(Rotation.R270.getAffineTransform(ResourceManager.NORMALIZED_SIZE));
        BRIDGE_AREA_WE = a;
    }

    private final double imageSizeRatio;
    private final Map<FeatureDescriptor, FeatureArea> areas = new HashMap<>();
    private final Map<String, Area> subtractionAll = new HashMap<>(); //key tile ID
    private final Map<String, Area> subtractionFarm = new HashMap<>(); //key tile ID
    private final Set<FeatureDescriptor> complementFarms = new HashSet<>();
    private final Map<FeatureDescriptor, ImmutablePoint> points;

    public ThemeGeometry(ClassLoader loader, String folder, double imageSizeRatio) throws IOException, SAXException, ParserConfigurationException {
        this.imageSizeRatio = imageSizeRatio;

        NodeList nl;
        Element shapes = XMLUtils.parseDocument(loader.getResource(folder +"/shapes.xml")).getDocumentElement();
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

    public double getImageSizeRatio() {
        return imageSizeRatio;
    }

    private FeatureDescriptor createFeatureDescriptor(String featureName, String tileAndLocation) {
        String[] tokens = tileAndLocation.split(" ");
        return FeatureDescriptor.valueOf(tokens[0], featureName, tokens[1]);
    }

    private AreaWithZIndex createArea(Element shapeNode) {
        Integer zIndex = XMLUtils.attributeIntValue(shapeNode, "zIndex");
        NodeList nl = shapeNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element el = (Element) nl.item(i);
                if (el.getNodeName().startsWith("svg:")) {
                    Area area = new SvgToShapeConverter().convert(el);
                    return new AreaWithZIndex(area, zIndex);
                }
            }
        }
        throw new IllegalArgumentException("Node doesn't contains svg shape.");
    }

    private int getZIndex(Integer explicit, FeatureDescriptor fd) {
        if (explicit != null) {
            return explicit;
        }
        Class<? extends Feature> ft = fd.getFeatureType();
        if (ft != null) {
            if (Road.class.isAssignableFrom(ft)) return FeatureArea.DEFAULT_ROAD_ZINDEX;
            if (City.class.isAssignableFrom(ft)) return FeatureArea.DEFAULT_CITY_ZINDEX;
            if (Farm.class.isAssignableFrom(ft)) return FeatureArea.DEFAULT_FARM_ZINDEX;
        }
        return FeatureArea.DEFAULT_STRUCTURE_ZINDEX;
    }

    private void processShapeElement(Element shapeNode) {
        final AreaWithZIndex az = createArea(shapeNode);

        SvgTransformationCollector transformCollector = new SvgTransformationCollector(shapeNode, imageSizeRatio);
        transformCollector.collect(new GeometryHandler() {

            @Override
            public void processApply(Element node, FeatureDescriptor fd, AffineTransform transform, AreaRotationScaling rotationScaling) {
                assert !areas.containsKey(fd) : "Duplicate key " + fd;
                FeatureArea area = new FeatureArea(az.area.createTransformedArea(transform), getZIndex(az.zIndex, fd));
                area = area.setRotationScaling(rotationScaling);
                areas.put(fd, area);
            }

            @Override
            public void processSubstract(Element node, String tileId, AffineTransform transform, boolean isFarm) {
                Map<String, Area> target = isFarm ? subtractionFarm : subtractionAll;
                Area transformed = az.area.createTransformedArea(transform);
                if (target.containsKey(tileId)) {
                    transformed.add(target.get(tileId));
                }
                target.put(tileId, transformed);

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

    private FeatureDescriptor[] getLookups(String tileId, Class<? extends Feature> featureType, Location location) {
        FeatureDescriptor[] fd = new FeatureDescriptor[2];
        fd[0] = new FeatureDescriptor(tileId, featureType, location);
        fd[1] = new FeatureDescriptor(FeatureDescriptor.EVERY, featureType, location);
        return fd;
    }

    public FeatureArea getArea(String tileId, Class<? extends Feature> featureClass, Location loc) {
        FeatureArea fa;
        if (featureClass != null && featureClass.equals(Bridge.class)) {
            Area a = getBridgeArea(loc);
            fa = new FeatureArea(a, FeatureArea.DEFAULT_BRIDGE_ZINDEX);
            fa = fa.setFixed(true);
            return fa;
        }
        FeatureDescriptor lookups[] = getLookups(tileId, featureClass, loc);

        for (FeatureDescriptor fd : lookups) {
            fa = areas.get(fd);
            if (fa != null) return fa;
        }
        return null;
    }

    public Area getBridgeArea(Location loc) {
        //TODO use shapes.xml to define areas ? (but it is too complicated shape)
        if (loc == Location.NS) return BRIDGE_AREA_NS;
        if (loc == Location.WE) return BRIDGE_AREA_WE;
        throw new IllegalArgumentException("Incorrect location");
    }

    public Area getSubtractionArea(String tileId, boolean isFarm) {
        if (isFarm) {
            Area area = getSubtractionArea(subtractionFarm, tileId);
            if (area != null) return area;
        }
        return getSubtractionArea(subtractionAll, tileId);
    }

    private Area getSubtractionArea(Map<String, Area> subtractions, String tileId) {
        Area area = subtractions.get(tileId);
        return area;
    }

    public Boolean isFarmComplement(String tileId, Location loc) {
        FeatureDescriptor lookups[] = getLookups(tileId, Farm.class, loc);
        for (FeatureDescriptor fd : lookups) {
            if (areas.containsKey(fd)) return Boolean.FALSE;
        }
        for (FeatureDescriptor fd : lookups) {
            if (complementFarms.contains(fd)) return Boolean.TRUE;
        }
        return null;
    }

    public ImmutablePoint getMeeplePlacement(String tileId, Class<? extends Feature> feature, Location location) {
        FeatureDescriptor lookups[] = getLookups(tileId, feature, location);
        ImmutablePoint point = null;
        for (FeatureDescriptor fd : lookups) {
            point = points.get(fd);
            if (point != null) break;
        }
        return point;
    }
}