package com.jcloisterzone.ui.resources.svg;

import static com.jcloisterzone.ui.plugin.ResourcePlugin.NORMALIZED_SIZE;

import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.resources.AreaRotationScaling;
import com.jcloisterzone.ui.resources.FeatureDescriptor;

public class SvgTransformationCollector {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Element root;

    private final Location baseLocation;
    private final double imageSizeRatio;
    private final boolean isRectangular;
    private final AreaRotationScaling rotationScaling;

    private ArrayDeque<AffineTransform> transforms = new ArrayDeque<>();

    public SvgTransformationCollector(Element root, double imageSizeRatio) {    	
    	this.imageSizeRatio = imageSizeRatio;
    	this.isRectangular = Math.abs(imageSizeRatio - 1.0) > 0.00001;
    	this.rotationScaling = AreaRotationScaling.fromXmlAttr(root.getAttribute("noScale"));
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
            logger.error("Invalid geometry definition:\n" + XMLUtils.nodeToString(root), ex);
        }
    }
    
    private void concatenateRotation(AffineTransform af, Rotation rot) {
    	if (Rotation.R0 == rot) {
    		return;
    	}
    	if (isRectangular) {
    		if (rot == Rotation.R90 || rot == Rotation.R270) {
    			rotationScaling.concatAffineTransform(af, imageSizeRatio);	    		
    		}
            af.concatenate(AffineTransform.getScaleInstance(1.0, imageSizeRatio));                    
            af.concatenate(rot.getAffineTransform(NORMALIZED_SIZE));
            af.concatenate(AffineTransform.getScaleInstance(1.0, 1.0/imageSizeRatio));                       
        } else {
        	af.concatenate(rot.getAffineTransform(NORMALIZED_SIZE));
        }
    }
    
    private AreaRotationScaling getEffectiveRotationScaling(Rotation rot) {    	
    	if (rot == Rotation.R90 || rot == Rotation.R270) {
    		return rotationScaling.reverse();
    	}
    	return rotationScaling;
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
                AreaRotationScaling ars = rotationScaling;
                if (baseLocation != null) {
                    Rotation rot = fd.getLocation().getRotationOf(baseLocation);
                    concatenateRotation(af, rot);
                    ars = getEffectiveRotationScaling(rot);
                }
                handler.processApply(child, fd, af, ars);
                if (XMLUtils.attributeBoolValue(child, "allRotations")) {
                	assert baseLocation == null : "baseLocation is not allowed together with allRotations attribute";
                    Rotation rot = Rotation.R90;
                    for (int ri = 0; ri < 3; ri++) {
                    	ars = getEffectiveRotationScaling(rot);
                    	AffineTransform afCpy = new AffineTransform(af);
                        Location rotatedLoc = fd.getLocation().rotateCW(rot);
                        FeatureDescriptor rotatedFd = new FeatureDescriptor(fd.getTileId(), fd.getFeatureType(), rotatedLoc);
                        concatenateRotation(afCpy, rot);                        
                        handler.processApply(child, rotatedFd, afCpy, ars);
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

    /* HACK hardcoded possible values */
    private AffineTransform createTransformation(String svg) {
        switch (svg) {
        case "rotate(90 500 500)": 
        	return AffineTransform.getRotateInstance(Math.PI * 0.5, 500, 500);
        case "rotate(180 500 500)": 
        	return AffineTransform.getRotateInstance(Math.PI, 500, 500);
        case "rotate(270 500 500)": 
        	return AffineTransform.getRotateInstance(Math.PI * 1.5, 500, 500);
        case "translate(1000,0) scale(-1, 1)":
        	AffineTransform af = AffineTransform.getTranslateInstance(1000, 0);
        	af.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
        	return af;
        default: throw new IllegalArgumentException("Unsupported transform: "+svg);
        }
    }


    public interface GeometryHandler {
        public void processApply(Element node, FeatureDescriptor fd, AffineTransform transform, AreaRotationScaling rotScaling);
        public void processSubstract(Element node, String tileId, AffineTransform transform, boolean isFarm);
    }

}
