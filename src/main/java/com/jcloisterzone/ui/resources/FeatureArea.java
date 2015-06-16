package com.jcloisterzone.ui.resources;

import java.awt.geom.Area;

import com.jcloisterzone.board.Location;

public class FeatureArea {

    public final static int DEFAULT_FARM_ZINDEX = 10;
    public final static int DEFAULT_CITY_ZINDEX = 20;
    public final static int DEFAULT_ROAD_ZINDEX = 30;
    public final static int DEFAULT_STRUCTURE_ZINDEX = 40;
    public final static int DEFAULT_BRIDGE_ZINDEX = 50;

    private Location loc;
    private Area area;
    private int zIndex;

    public FeatureArea(Location loc, Area area, int zIndex) {
        this.loc = loc;
        this.area = area;
        this.zIndex = zIndex;
    }

    public FeatureArea(FeatureArea copy) {
        this.loc = copy.loc;
        this.area = new Area(copy.area);
        this.zIndex = copy.zIndex;
    }

    public Location getLoc() {
        return loc;
    }

    public Area getArea() {
        return area;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }
}
