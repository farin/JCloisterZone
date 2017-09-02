package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.game.ScoringResult;
import com.jcloisterzone.game.state.GameState;

public class ScoreCastle extends ScoreFeature implements ScoringResult {

    private final int points;

    public ScoreCastle(Castle feature, int points) {
        super(feature);
        this.points = points;
    }

    @Override
    protected int getFeaturePoints(GameState state, Player player) {
        return points;
    }

    public int getPoints() {
        return points;
    }

}
