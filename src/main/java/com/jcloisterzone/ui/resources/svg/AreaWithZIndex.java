package com.jcloisterzone.ui.resources.svg;

import java.awt.geom.Area;

public class AreaWithZIndex {
    Area area;
    Integer zIndex;

    public AreaWithZIndex(Area area, Integer zIndex) {
        this.area = area;
        this.zIndex = zIndex;
    }
}