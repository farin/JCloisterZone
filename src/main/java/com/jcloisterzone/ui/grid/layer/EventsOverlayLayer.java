package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.collection.Vector;

public class EventsOverlayLayer extends AbstractGridLayer {


    private GameState state;
    private Feature highlightedFeature;
    private Vector<Position> highlightedPositions;


    public EventsOverlayLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void paint(Graphics2D g2) {
        if (highlightedPositions == null && highlightedFeature == null)  {
            return;
        }

        g2.setColor(gc.getClient().getTheme().getBoardOverlay());

        Area wholeGrid = new Area(new Rectangle(
           -gridPanel.getOffsetX(),
           -gridPanel.getOffsetY(),
           gridPanel.getWidth(),
           gridPanel.getHeight()
        ));
        if (highlightedPositions != null) {
            int sx = gridPanel.getTileHeight();
            int sy = gridPanel.getTileHeight();
            for (Position pos : highlightedPositions) {
                wholeGrid.subtract(new Area(new Rectangle(pos.x * sx, pos.y * sy, sx, sy)));
            }
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
        this.highlightedPositions = null;
        gridPanel.repaint();
    }

    public Feature getHighlightedFeature() {
        return highlightedFeature;
    }

    public void setHighlightedFeature(GameState state, Feature highlightedFeature) {
        this.state = state;
        this.highlightedFeature = highlightedFeature;
        this.highlightedPositions = null;
        gridPanel.repaint();
    }

    public Vector<Position> getHighlightedPositions() {
        return highlightedPositions;
    }

    public void setHighlightedPositions(GameState state, Vector<Position> highlightedPosition) {
        this.state = state;
        this.highlightedFeature = null;
        this.highlightedPositions = highlightedPosition;
        gridPanel.repaint();
    }


}
