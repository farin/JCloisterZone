package com.jcloisterzone.ui.theme;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.ui.plugin.ResourcePlugin;


public class AreaProvider {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    //TODO descriptor type!
    private final Map<FeatureDescriptor, Area> areas = Maps.newHashMap();
    private final Map<String, Area> substraction = Maps.newHashMap(); //key tile ID
    private final Set<FeatureDescriptor> complementFarms = Sets.newHashSet();

    private static final Area BRIDGE_AREA_NS, BRIDGE_AREA_WE;

    static {
        Area a = new Area(new Rectangle(400, 0, 200, 1000));
        a.subtract(new Area(new Ellipse2D.Double(300, 150, 200, 700)));
        BRIDGE_AREA_NS = new Area(a);
        a.transform(Rotation.R270.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
        BRIDGE_AREA_WE = a;
    }

    public AreaProvider(URL areaDef) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Element display = docBuilder.parse(areaDef.openStream()).getDocumentElement();
            NodeList nl = display.getElementsByTagName("shape");
            for (int i = 0; i < nl.getLength(); i++) {
                processShapeElement((Element) nl.item(i));
            }
            nl = display.getElementsByTagName("complement-farm");
            for (int i = 0; i < nl.getLength(); i++) {
                processComplementFarm((Element) nl.item(i));
            }

        } catch (Exception e) {
            logger.error("Unable to read theme definitions from " + areaDef.toString(), e);
            System.exit(1);
        }
    }

    private FeatureDescriptor createFeatureDescriptor(String featureName, String tileAndLocation) {
        String[] tokens = tileAndLocation.split(" ");
        return FeatureDescriptor.valueOf(tokens[0], featureName, tokens[1]);
    }

    private void processApplyAndSubstractChilds(Element node, SvgToShapeConverter shapeConverter, String featureName, Location baseLocation) {
        NodeList nl = node.getElementsByTagName("apply");
        //Area[] areas = new Area[4];
        for (int i = 0; i < nl.getLength(); i++) {
            Element applyElemenet = (Element) nl.item(i);
            String elementTransform = applyElemenet.hasAttribute("svg:transform") ? applyElemenet.getAttribute("svg:transform") : null;
            assert elementTransform == null || shapeConverter.getTransformation() == null;
            FeatureDescriptor desc = createFeatureDescriptor(featureName, applyElemenet.getTextContent());
            Area area = shapeConverter.convert(elementTransform);
            if (baseLocation != null) {
                Rotation rotate = desc.getLocation().getRotationOf(baseLocation);
                area = area.createTransformedArea(rotate.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
            }
            areas.put(desc, area);
        }
        nl = node.getElementsByTagName("substract");
        for (int i = 0; i < nl.getLength(); i++) {
            Element substractElement = (Element) nl.item(i);
            String tileId = substractElement.getTextContent().trim();
            //TODO merge if already exists
            substraction.put(tileId, shapeConverter.convert());
        }
    }

    private void processShapeElement(Element shapeNode) {
        SvgToShapeConverter shapeConverter = null;
        Location baseLocation = null;
        String featureName = shapeNode.getAttribute("feature");
        NodeList nl;

        nl = shapeNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element el = (Element) nl.item(i);
                if (el.getNodeName().startsWith("svg:")) {
                    shapeConverter = new SvgToShapeConverter(el);
                    break;
                }
            }
        }
        if (shapeNode.hasAttribute("baseLocation")) {
            baseLocation = Location.valueOf(shapeNode.getAttribute("baseLocation"));
        }
        processApplyAndSubstractChilds(shapeNode, shapeConverter, featureName, baseLocation);
        nl = shapeNode.getElementsByTagName("g");
        assert baseLocation == null || nl.getLength() == 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Element gNode = (Element) nl.item(i);
            shapeConverter.setTransformation(gNode.getAttribute("svg:transform"));
            processApplyAndSubstractChilds(gNode, shapeConverter, featureName, null);
        }
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
        loc = loc.rotateCCW(tile.getRotation());
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
}