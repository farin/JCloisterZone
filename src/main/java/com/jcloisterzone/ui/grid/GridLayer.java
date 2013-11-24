package com.jcloisterzone.ui.grid;

import java.awt.Graphics2D;
import java.util.Comparator;

public interface GridLayer {

    void paint(Graphics2D g2);

    int getZIndex();

    void zoomChanged(int squareSize);

    // void gridChanged(int left, int right, int top, int bottom);

    void layerAdded();

    void layerRemoved();

    public static final Comparator<GridLayer> Z_INDEX_COMPARATOR = new Comparator<GridLayer>() {
        @Override
        public int compare(GridLayer o1, GridLayer o2) {
            if (o1.getZIndex() < o2.getZIndex())
                return -1;
            if (o1.getZIndex() > o2.getZIndex())
                return 1;
            return 0;
        }
    };

}
