package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;

public abstract class AbstractTilePlacementLayer extends AbstractGridLayer implements GridMouseListener {

    protected static final Composite ALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
    protected static final Composite DISALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);

    private ImmutableSet<Position> availablePositions;

    private Position previewPosition;
    private Image previewIcon;


    public AbstractTilePlacementLayer(GridPanel gridPanel, Set<Position> positions) {
        super(gridPanel);
        availablePositions = ImmutableSet.copyOf(positions);
    }

    @Override
    public int getZIndex() {
        return 3;
    }

    public Position getPreviewPosition() {
        return previewPosition;
    };

    abstract protected void drawPreviewIcon(Graphics2D g2, Image previewIcon, Position pos);
    abstract protected Image createPreviewIcon();


    @Override
    public void paint(Graphics2D g2) {
        int borderSize = getSquareSize() - 4,
            shift = 2,
            thickness = borderSize/14;

        g2.setColor(Color.LIGHT_GRAY);
        for (Position p : availablePositions) {
            if (previewPosition == null || !previewPosition.equals(p)) {
                int x = getOffsetX(p)+shift, y = getOffsetY(p)+shift;
                g2.fillRect(x, y, borderSize, thickness);
                g2.fillRect(x, y+borderSize-thickness, borderSize, thickness);
                g2.fillRect(x, y, thickness, borderSize);
                g2.fillRect(x+borderSize-thickness, y, thickness, borderSize);
            }
        }

        if (previewPosition != null) {
            if (previewIcon == null) {
                previewIcon = createPreviewIcon();
            }
            drawPreviewIcon(g2, previewIcon, previewPosition);
        }
        g2.setColor(getClient().isClientActive() ? Color.BLACK : Color.GRAY);

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