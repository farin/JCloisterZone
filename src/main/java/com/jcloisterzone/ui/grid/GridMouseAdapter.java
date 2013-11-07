package com.jcloisterzone.ui.grid;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    private Position getGridPosition(MouseEvent e) {
        int sqSize = gridPanel.getSquareSize();
        int clickX = e.getX() - gridPanel.getOffsetX();
        int clickY = e.getY() - gridPanel.getOffsetY();
        int x = clickX / sqSize + ((clickX < 0) ? -1 : 0);
        int y = clickY / sqSize + ((clickY < 0) ? -1 : 0);
        return new Position(x, y);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Position p = getGridPosition(e);
        if (currentPosition != null && ! currentPosition.equals(p)) {
            listener.squareExited(e, currentPosition);
            currentPosition = null;
        }
        if (p != null && ! p.equals(currentPosition)) {
            currentPosition = p;
            listener.squareEntered(e, currentPosition);
        }
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
            listener.squareExited(e, currentPosition);
            currentPosition = null;
        }

    }

}
