package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;

public class ScoreEvent extends Event {

    //TODO fields revision

    private final Feature feature;

    private final int points;
    private final String label;
    private final Meeple meeple;
    private final boolean isFinal;

    public ScoreEvent(Feature feature, int points, String label, Meeple meeple, boolean isFinal) {
        super(meeple == null ? null : meeple.getPlayer(), feature.getTile().getPosition(), feature.getLocation());
        this.feature = feature;
        this.points = points;
        this.label = label;
        this.meeple = meeple;
        this.isFinal = isFinal;
    }

    public ScoreEvent(Position position, Player player, int points, String label, boolean isFinal) {
        super(player, position);
        this.feature = null;
        this.meeple = null;
        this.points = points;
        this.label = label;
        this.isFinal = isFinal;
    }

    public Feature getFeature() {
        return feature;
    }

    public int getPoints() {
        return points;
    }

    public String getLabel() {
        return label;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public boolean isFinal() {
        return isFinal;
    }


}
