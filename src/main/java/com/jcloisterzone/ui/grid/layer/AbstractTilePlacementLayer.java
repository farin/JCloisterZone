package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;

public abstract class AbstractTilePlacementLayer extends AbstractGridLayer implements GridMouseListener {

    protected static final Composite ALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
    protected static final Composite DISALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);

    private boolean active;
    protected boolean playersAid;
    private Set<Position> availablePositions;
    private Set<Position> occupiedPositions;

    private Position previewPosition;
    private Image previewIcon;


    public AbstractTilePlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    public void setAvailablePositions(Set<Position> availablePositions) {
        this.availablePositions = availablePositions;
    }
    
    void setOccupiedPositions(Set<Position> occupiedPositions) {
    	this.occupiedPositions = occupiedPositions;
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

    public void setPlayersAid(boolean playersAid) {
    	this.playersAid = playersAid;
    }


    abstract protected void drawPreviewIcon(Graphics2D g2, Image previewIcon, Position pos);
    abstract protected Image createPreviewIcon();


    @Override
    public void onHide() {
        super.onHide();
        previewIcon = null;
        availablePositions = null;
        previewPosition = null;
    }


    @Override
    public void paint(Graphics2D g2) {
        if (availablePositions == null) return;
        int borderSize = getSquareSize() - 4,
            shift = 2,
            thickness = playersAid ? borderSize/14 : 0;

        g2.setColor(getClient().getTheme().getTilePlacementColor());
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
        g2.setColor(active ? Color.BLACK : Color.GRAY);

    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        if ((!playersAid && !occupiedPositions.contains(p)) || availablePositions.contains(p)) {
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