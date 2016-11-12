package com.jcloisterzone.ui.resources;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class FeatureArea {

    public final static int DEFAULT_FARM_ZINDEX = 10;
    public final static int DEFAULT_CITY_ZINDEX = 20;
    public final static int DEFAULT_ROAD_ZINDEX = 30;
    public final static int DEFAULT_STRUCTURE_ZINDEX = 40;
    public final static int DEFAULT_BRIDGE_ZINDEX = 50;

    private Area trackingArea; //mouse tracking area
    private Area displayArea; //mouse tracking area
    private int zIndex;
    private Color forceAreaColor;
    private AreaRotationScaling rotationScaling = AreaRotationScaling.NORMAL;

    public FeatureArea(Area trackingArea, int zIndex) {
        this.trackingArea = trackingArea;
        this.zIndex = zIndex;
    }

    public FeatureArea(Area trackingArea, Area displayArea, int zIndex) {
        this(trackingArea, zIndex);
        this.displayArea = displayArea;
    }

    public FeatureArea(FeatureArea copy) {
        this.trackingArea = new Area(copy.trackingArea);
        if (copy.displayArea != null) {
            this.displayArea = new Area(copy.displayArea);
        }
        this.zIndex = copy.zIndex;
    }
    
    public FeatureArea transform(AffineTransform t) {
    	FeatureArea cp = new FeatureArea(this);
    	cp.getTrackingArea().transform(t);
    	if (cp.getDisplayArea() != null) {
    		cp.getDisplayArea().transform(t);
    	}
    	return cp;
    }

    public Area getTrackingArea() {
        return trackingArea;
    }

    public Area getDisplayArea() {
        return displayArea;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setTrackingArea(Area area) {
        this.trackingArea = area;
    }

    public void setDisplayArea(Area displayArea) {
        this.displayArea = displayArea;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public Color getForceAreaColor() {
        return forceAreaColor;
    }

    public void setForceAreaColor(Color forceAreaColor) {
        this.forceAreaColor = forceAreaColor;
    }
    
    public AreaRotationScaling getRotationScaling() {
		return rotationScaling;
	}

	public void setRotationScaling(AreaRotationScaling rotationScaling) {
		this.rotationScaling = rotationScaling;
	}

	@Override
    public String toString() {
        return zIndex + "/" + trackingArea.toString();
    }
}
