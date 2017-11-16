package com.jcloisterzone.ui.grid.eventpanel;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.Feature;

import io.vavr.collection.Vector;

public abstract class EventItem {
    private final PlayEvent event;
    private final Color turnColor;
    private final Color color;

    private Feature highlightedFeature;
    private Vector<Position> highlightedPositions;

    public EventItem(PlayEvent event, Color turnColor, Color color) {
        super();
        this.event = event;
        this.turnColor = turnColor;
        this.color = color;
    }

    public abstract void draw(Graphics2D g2);
    //public abstract

    public Feature getHighlightedFeature() {
        return highlightedFeature;
    }

    public void setHighlightedFeature(Feature highlightedFeature) {
        this.highlightedFeature = highlightedFeature;
    }

    public Vector<Position> getHighlightedPositions() {
        return highlightedPositions;
    }

    public void setHighlightedPositions(Vector<Position> highlightedPositions) {
        this.highlightedPositions = highlightedPositions;
    }

    public PlayEvent getEvent() {
        return event;
    }

    public Color getTurnColor() {
        return turnColor;
    }

    public Color getColor() {
        return color;
    }


}
