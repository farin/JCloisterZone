package com.jcloisterzone.ui.resources;

import static com.jcloisterzone.ui.plugin.ResourcePlugin.NORMALIZED_SIZE;

import java.awt.geom.AffineTransform;

public enum AreaRotationScaling {
	
	NORMAL,
	NO_SCALE_WIDTH,
	NO_SCALE_HEIGHT;
	
	public AreaRotationScaling reverse() {
		switch (this) {
		case NORMAL: return NORMAL;
		case NO_SCALE_WIDTH: return NO_SCALE_HEIGHT;
		case NO_SCALE_HEIGHT: return NO_SCALE_WIDTH;
		}
		throw new IllegalArgumentException();
	}
	
	public void concatAffineTransform(AffineTransform af, double imageSizeRatio) {
		if (this == NORMAL) {
			return;
		}
		double tx = NORMALIZED_SIZE/2.0;
		double ty = tx*imageSizeRatio;
		
		af.concatenate(AffineTransform.getTranslateInstance(tx, ty));
		if (this == NO_SCALE_WIDTH) {    			    		
            af.concatenate(AffineTransform.getScaleInstance(imageSizeRatio, 1.0));                            
		} else {
			af.concatenate(AffineTransform.getScaleInstance(1.0, imageSizeRatio));
		}
		af.concatenate(AffineTransform.getTranslateInstance(-tx, -ty));		
	}
		
	
	public static AreaRotationScaling fromXmlAttr(String attr) {
		if ("width".equals(attr)) return NO_SCALE_WIDTH;
		if ("height".equals(attr)) return NO_SCALE_HEIGHT;
		return NORMAL;		
	}
}
