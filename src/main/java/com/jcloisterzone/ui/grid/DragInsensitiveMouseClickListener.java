package com.jcloisterzone.ui.grid;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public abstract class DragInsensitiveMouseClickListener implements MouseListener {

    private final int allowedTravel;

    public Point mouseDownPoint;

    public DragInsensitiveMouseClickListener(int allowedTravel) {
        this.allowedTravel = allowedTravel;
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        mouseDownPoint = e.getPoint();
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        double horizontalTravel = Math.abs(mouseDownPoint.getX() - e.getX());
        double verticalTravel = Math.abs(mouseDownPoint.getY() - e.getY());

        if (horizontalTravel < allowedTravel && verticalTravel < allowedTravel) {
            mouseClicked(e);
        }
    }

}
