package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;

public class ScoreCastle extends ScoreFeature implements ScoreFeatureReducer {

    private final int points;

    public ScoreCastle(Castle feature, int points, boolean isFinal) {
        super(feature, isFinal);
        this.points = points;
    }

    @Override
    protected int getFeaturePoints(GameState state, Player player) {
        return points;
    }

    @Override
    public int getFeaturePoints() {
        return points;
    }
}
