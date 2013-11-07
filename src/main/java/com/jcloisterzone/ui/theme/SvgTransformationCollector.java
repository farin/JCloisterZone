package com.jcloisterzone.ui.theme;

import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.plugin.ResourcePlugin;

public class SvgTransformationCollector {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Element root;

    private final Location baseLocation;

    private ArrayDeque<AffineTransform> transforms = new ArrayDeque<>();

    public SvgTransformationCollector(Element root) {
        this.root = root;
        if (root.hasAttribute("baseLocation")) {
            baseLocation = Location.valueOf(root.getAttribute("baseLocation"));
        } else {
            baseLocation = null;
        }
    }

    public void collect(GeometryHandler handler) {
        try {
            collect(root, handler);
        } catch (Exception ex) {
            logger.error("Invalid geometry definition:\n" + XmlUtils.nodeToString(root), ex);
        }
    }

    private void collect(Element parent, GeometryHandler handler) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (!(nl.item(i) instanceof Element)) continue;
            Element child = (Element) nl.item(i);
            boolean hasTransform = child.hasAttribute("svg:transform");
            if (hasTransform) {
                transforms.push(createTransformation(child.getAttribute("svg:transform")));
            }
            switch (child.getNodeName()) {
            case "apply":
                String[] tokens = child.getTextContent().split(" ");
                FeatureDescriptor fd =  FeatureDescriptor.valueOf(tokens[0], root.getAttribute("feature"), tokens[1]);
                AffineTransform af = getTransform();
                if (baseLocation != null) {
                    Rotation rotate = fd.getLocation().getRotationOf(baseLocation);
                    af.concatenate(rotate.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
                }
                handler.processApply(child, fd, af);
                if (XmlUtils.attributeBoolValue(child, "allRotations")) {
                    Rotation rot = Rotation.R90;
                    for (int ri = 0; ri < 3; ri++) {
                        Location rotatedLoc = fd.getLocation().rotateCW(rot);
                        FeatureDescriptor rotatedFd = new FeatureDescriptor(fd.getTileId(), fd.getFeatureType(), rotatedLoc);
                        af.concatenate(Rotation.R90.getAffineTransform(ResourcePlugin.NORMALIZED_SIZE));
                        handler.processApply(child, rotatedFd, af);
                        rot = rot.next();
                    }
                }
                break;
            case "substract":
                assert baseLocation == null : "baseLocation is not allowed together with substraction element";
                String feature = root.getAttribute("feature");
                assert feature.equals("") || feature.equals("FARM") : "Substraction area can be declared only generic or for FARM";
                handler.processSubstract(child, child.getTextContent(), getTransform(), feature.equals("FARM"));
                break;
            case "g":
                assert baseLocation == null;
                collect(child, handler);
                break;
            }
            if (hasTransform) {
                transforms.pop();
            }
        }
    }

    private AffineTransform getTransform() {
        AffineTransform af = new AffineTransform();
        for (AffineTransform item : transforms) {
            af.concatenate(item);
        }
        return af;
    }

    private AffineTransform createTransformation(String svg) {
        switch (svg) {
        case "rotate(90 500 500)": return AffineTransform.getRotateInstance(Math.PI * 0.5, 500, 500);
        case "rotate(180 500 500)": return AffineTransform.getRotateInstance(Math.PI, 500, 500);
        case "rotate(270 500 500)": return AffineTransform.getRotateInstance(Math.PI * 1.5, 500, 500);
        default: throw new IllegalArgumentException("Unsupported transform: "+svg);
        }
    }


    public interface GeometryHandler {
        public void processApply(Element node, FeatureDescriptor fd, AffineTransform transform);
        public void processSubstract(Element node, String tileId, AffineTransform transform, boolean isFarm);
    }

}
