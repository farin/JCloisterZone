package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;

public abstract class AbstractTilePlacementLayer extends AbstractGridLayer implements GridMouseListener {

    protected static final Composite ALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
    protected static final Composite DISALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);

    private boolean active;
    private Set<Position> availablePositions;

    private Position previewPosition;

    public AbstractTilePlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    public void setAvailablePositions(Set<Position> availablePositions) {
        this.availablePositions = availablePositions;
    }

    public Position getPreviewPosition() {
        return previewPosition;
    };

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    abstract protected void drawPreviewIcon(Graphics2D g2, Position pos);

    @Override
    public void onHide() {
        super.onHide();
        availablePositions = null;
        previewPosition = null;
    }


    @Override
    public void paint(Graphics2D g2) {
        if (availablePositions == null) return;
        int xSize = getTileWidth() - 4,
            ySize = getTileHeight() - 4,
            shift = 2,
            thickness = xSize/14;

        g2.setColor(getClient().getTheme().getTilePlacementColor());
        for (Position p : availablePositions) {
            if (previewPosition == null || !previewPosition.equals(p)) {
                int x = getOffsetX(p)+shift, y = getOffsetY(p)+shift;
                g2.fillRect(x, y, xSize, thickness);
                g2.fillRect(x, y+ySize-thickness, xSize, thickness);
                g2.fillRect(x, y, thickness, ySize);
                g2.fillRect(x+xSize-thickness, y, thickness, ySize);
            }
        }

        if (previewPosition != null) {
            drawPreviewIcon(g2, previewPosition);
        }
        g2.setColor(active ? Color.BLACK : Color.GRAY);

    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        if (availablePositions.contains(p)) {
            previewPosition = p;
            gridPanel.repaint();
        }
    }

    @Override
    public void squareExited(MouseEvent e, Position p) {
        if (previewPosition != null) {
            previewPosition = null;
            gridPanel.repaint();
        }
    }
}