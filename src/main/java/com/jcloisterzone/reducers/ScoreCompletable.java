package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.game.state.GameState;

public class ScoreCompletable extends ScoreFeature {

    private int points = Integer.MIN_VALUE;

    public ScoreCompletable(Completable feature) {
        super(feature);
    }

    /* don't recompute points if they are already known */
    public ScoreCompletable(Completable feature, int points) {
        super(feature);
        this.points = points;
    }

    @Override
    int getFeaturePoints(GameState state, Player player) {
        return points;
    }

    @Override
    public Completable getFeature() {
        return (Completable) super.getFeature();
    }

    @Override
    public GameState apply(GameState state) {
        if (points == Integer.MIN_VALUE) {
            points = getFeature().getPoints(state);
        }
        return super.apply(state);
    }

}
