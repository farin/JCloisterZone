package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FairyCapability;

public class ScoreEvent extends PlayEvent implements Undoable {

    //TODO fields revision

    private final Feature feature;
    private final Position position;

    private final int points;
    private final PointCategory category;
    private final Class<? extends Meeple> meepleType;

    private String label;
    private boolean isFinal;

    public ScoreEvent(Feature feature, int points, PointCategory category, Meeple meeple) {
        super(meeple == null ? null : meeple.getPlayer());
        this.feature = feature;
        this.position = feature.getTile().getPosition();
        this.points = points;
        this.category = category;
        this.meepleType = meeple.getClass();
    }

    public ScoreEvent(Position position, Player player, int points, PointCategory category) {
        super(player);
        this.position = position;
        this.feature = null;
        this.meepleType = null;
        this.points = points;
        this.category = category;
    }

    public Feature getFeature() {
        return feature;
    }

    public Position getPosition() {
        return position;
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

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
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
        if (label != null && label.contains(" + ")) {
            //HACK: nasty hack, fairy finished object fires score event as one, but points are in two categories
            getPlayer().addPoints(-FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY);
            getPlayer().addPoints(-points+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, category);
        } else {
            getPlayer().addPoints(-points, category);
        }
    }
}
