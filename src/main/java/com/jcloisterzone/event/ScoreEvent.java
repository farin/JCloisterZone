package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class ScoreEvent extends PlayEvent implements Undoable {

    //TODO fields revision

    private final Feature feature;

    private final int points;
    private final PointCategory category;
    private final Meeple meeple;

    private String label;
    private boolean isFinal;

    public ScoreEvent(Feature feature, int points, PointCategory category, Meeple meeple) {
        super(meeple == null ? null : meeple.getPlayer(), feature.getTile().getPosition(), feature.getLocation());
        this.feature = feature;
        this.points = points;
        this.category = category;
        this.meeple = meeple;
    }

    public ScoreEvent(Position position, Player player, int points, PointCategory category) {
        super(player, position);
        this.feature = null;
        this.meeple = null;
        this.points = points;
        this.category = category;
    }

    public Feature getFeature() {
        return feature;
    }

    public int getPoints() {
        return points;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label == null ? points + "" : label;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public PointCategory getCategory() {
        return category;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public void undo(Game game) {
        getPlayer().addPoints(-points, category);
    }
}
