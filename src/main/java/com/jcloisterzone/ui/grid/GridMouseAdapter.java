package com.jcloisterzone.ui.grid;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.event.MouseInputListener;

import com.jcloisterzone.board.Position;

public class GridMouseAdapter extends MouseAdapter implements MouseInputListener {

    final GridPanel gridPanel;
    final GridMouseListener listener;

    private Position currentPosition;

    public GridMouseAdapter(GridPanel gridPanel, GridMouseListener listener) {
        this.gridPanel = gridPanel;
        this.listener = listener;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    private Position getGridPosition(MouseEvent e) {
        int w = gridPanel.getTileWidth();
        int h = gridPanel.getTileHeight();
        Point2D point = gridPanel.getRelativePoint(e.getPoint());
        int clickX = (int) point.getX();
        int clickY = (int) point.getY();
        int x = clickX / w + ((clickX < 0) ? -1 : 0);
        int y = clickY / h + ((clickY < 0) ? -1 : 0);
        return new Position(x, y);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Position p = getGridPosition(e);
        if (currentPosition != null && ! currentPosition.equals(p)) {
            listener.tileExited(e, currentPosition);
            currentPosition = null;
        }
        if (p != null && ! p.equals(currentPosition)) {
            currentPosition = p;
            listener.tileEntered(e, currentPosition);
        }
        listener.mouseMoved(e, p);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Position p = getGridPosition(e);
        if (p != null) {
            listener.mouseClicked(e, p);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (currentPosition != null) {
            listener.tileExited(e, currentPosition);
            currentPosition = null;
        }

    }
}
