package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.state.GameState;

public class ScoreCastle extends ScoreFeature {

    private final int points;

    public ScoreCastle(Scoreable feature, int points) {
        super(feature);
        this.points = points;
    }

    @Override
    int getFeaturePoints(GameState state, Player player) {
        return points;
    }

}
