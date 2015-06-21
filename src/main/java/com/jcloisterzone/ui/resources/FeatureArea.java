package com.jcloisterzone.ui.resources;

import java.awt.geom.Area;

public class FeatureArea {

    public final static int DEFAULT_FARM_ZINDEX = 10;
    public final static int DEFAULT_CITY_ZINDEX = 20;
    public final static int DEFAULT_ROAD_ZINDEX = 30;
    public final static int DEFAULT_STRUCTURE_ZINDEX = 40;
    public final static int DEFAULT_BRIDGE_ZINDEX = 50;

    private Area area;
    private int zIndex;

    public FeatureArea(Area area, int zIndex) {
        this.area = area;
        this.zIndex = zIndex;
    }

    public FeatureArea(FeatureArea copy) {
        this.area = new Area(copy.area);
        this.zIndex = copy.zIndex;
    }

    public Area getArea() {
        return area;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    @Override
    public String toString() {
    	return zIndex + "/" + area.toString();
    }
}
