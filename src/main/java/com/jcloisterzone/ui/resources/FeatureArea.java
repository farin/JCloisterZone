package com.jcloisterzone.ui.resources;

import static com.jcloisterzone.ui.resources.ResourceManager.NORMALIZED_SIZE;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.ObjectInputStream.GetField;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.plugin.ResourcePlugin;

public class FeatureArea {

    public final static int DEFAULT_FARM_ZINDEX = 10;
    public final static int DEFAULT_CITY_ZINDEX = 20;
    public final static int DEFAULT_ROAD_ZINDEX = 30;
    public final static int DEFAULT_STRUCTURE_ZINDEX = 40;
    public final static int DEFAULT_BRIDGE_ZINDEX = 50;

    private final Area trackingArea; //mouse tracking area
    private final Area displayArea; //mouse tracking area
    private final int zIndex;
    private final Color forceAreaColor;
    private final AreaRotationScaling rotationScaling;
    private final boolean fixed; //do not rotate with tile

    public FeatureArea(Area trackingArea, int zIndex) {
        this(trackingArea, null, zIndex, null, AreaRotationScaling.NORMAL, false);
    }

    public FeatureArea(Area trackingArea, Area displayArea, int zIndex) {
        this(trackingArea, displayArea, zIndex, null, AreaRotationScaling.NORMAL, false);
    }

    private FeatureArea(Area trackingArea, Area displayArea, int zIndex, Color forceAreaColor, AreaRotationScaling rotationScaling, boolean fixed) {
        this.trackingArea = trackingArea;
        this.displayArea = displayArea;
        this.zIndex = zIndex;
        this.forceAreaColor = forceAreaColor;
        this.rotationScaling = rotationScaling;
        this.fixed = fixed;
    }

    public FeatureArea transform(AffineTransform t) {
        Area trackingArea = null, displayArea = null;
        if (this.trackingArea != null) {
            trackingArea  = this.trackingArea.createTransformedArea(t);
        }
        if (this.displayArea != null) {
            displayArea = this.displayArea.createTransformedArea(t);
        }
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling, fixed);
    }


    public FeatureArea translateTo(Position pos) {
        AffineTransform tx = AffineTransform.getTranslateInstance(pos.x * NORMALIZED_SIZE, pos.y * NORMALIZED_SIZE);
        return transform(tx);
    }

    public FeatureArea subtract(FeatureArea fa) {
        return subtract(fa.getTrackingArea());
    }

    public FeatureArea subtract(Area area) {
        Area trackingArea = null, displayArea = null;
        if (this.trackingArea != null) {
            trackingArea  = new Area(this.trackingArea);
            trackingArea.subtract(area);
        }
        if (this.displayArea != null) {
            displayArea = new Area(this.displayArea);
            displayArea.subtract(area);
        }
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling, fixed);
    }

    public Area getTrackingArea() {
        return trackingArea;
    }

    public Area getDisplayArea() {
        return displayArea == null ? trackingArea : displayArea;
    }

    public int getzIndex() {
        return zIndex;
    }

    public Color getForceAreaColor() {
        return forceAreaColor;
    }

    public AreaRotationScaling getRotationScaling() {
        return rotationScaling;
    }

    public boolean isFixed() {
        return fixed;
    }

    public FeatureArea setForceAreaColor(Color forceAreaColor) {
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling, fixed);
    }

    public FeatureArea setRotationScaling(AreaRotationScaling rotationScaling) {
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling, fixed);
    }

    public FeatureArea setFixed(boolean fixed) {
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling, fixed);
    }

    public FeatureArea setZIndex(int zIndex) {
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling, fixed);
    }

    @Override
    public String toString() {
        return zIndex + "/" + trackingArea.toString();
    }
}
