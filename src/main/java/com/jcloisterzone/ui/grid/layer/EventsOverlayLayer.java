package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

public class EventsOverlayLayer extends AbstractGridLayer {

    private static final Color OVERLAY = new Color(47, 79,  79, 185);

    private GameState state;
    private Feature highlightedFeature;
    private Position highlightedPosition;

    public EventsOverlayLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void paint(Graphics2D g2) {
        if (highlightedPosition == null && highlightedFeature == null)  {
            return;
        }

        g2.setColor(OVERLAY);

        Area wholeGrid = new Area(new Rectangle(
           -gridPanel.getOffsetX(),
           -gridPanel.getOffsetY(),
           gridPanel.getWidth(),
           gridPanel.getHeight()
        ));
        if (highlightedPosition != null) {
            int sx = gridPanel.getTileHeight();
            int sy = gridPanel.getTileHeight();
            wholeGrid.subtract(new Area(new Rectangle(
                highlightedPosition.x * sx,
                highlightedPosition.y * sy,
                sx,
                sy
            )));
        }
        if (highlightedFeature != null) {
            Area featureArea = getFeatureArea(state, highlightedFeature);
            featureArea = featureArea.createTransformedArea(getZoomScale());
            wholeGrid.subtract(featureArea);
        }

        g2.fill(wholeGrid);
    }

    public void clearHighlight() {
        this.state = null;
        this.highlightedFeature = null;
        this.highlightedPosition = null;
        gridPanel.repaint();
    }

    public Feature getHighlightedFeature() {
        return highlightedFeature;
    }

    public void setHighlightedFeature(GameState state, Feature highlightedFeature) {
        this.state = state;
        this.highlightedFeature = highlightedFeature;
        this.highlightedPosition = null;
        gridPanel.repaint();
    }

    public Position getHighlightedPosition() {
        return highlightedPosition;
    }

    public void setHighlightedPosition(GameState state, Position highlightedPosition) {
        this.state = state;
        this.highlightedFeature = null;
        this.highlightedPosition = highlightedPosition;
        gridPanel.repaint();
    }


}
