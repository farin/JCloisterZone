package com.jcloisterzone.ui.controls;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public class MouseListeningRegion {

    private final Rectangle region;
    private final RegionMouseListener listener;
    private final Object data;

    public MouseListeningRegion(Rectangle region, RegionMouseListener listener, Object data) {
        this.region = region;
        this.listener = listener;
        this.data = data;
    }

    public RegionMouseListener getListener() {
        return listener;
    }

    public Rectangle getRegion() {
        return region;
    }

    public Object getData() {
        return data;
    }

    public interface RegionMouseListener {
        public void mouseClicked(MouseEvent e, MouseListeningRegion origin);
    }

}
