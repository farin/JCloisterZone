package com.jcloisterzone.ui.grid;

import java.awt.Graphics2D;

public interface GridLayer {

    void paint(Graphics2D g2);

    void zoomChanged(int squareSize);

    boolean isVisible();
    void onShow();
    void onHide();
}
