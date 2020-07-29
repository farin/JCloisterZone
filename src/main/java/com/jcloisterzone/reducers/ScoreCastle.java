package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PointsExpression;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;

public class ScoreCastle extends ScoreFeature implements ScoreFeatureReducer {

    private final PointsExpression points;

    public ScoreCastle(Castle feature, PointsExpression points, boolean isFinal) {
        super(feature, isFinal);
        this.points = points;
    }

    @Override
    protected PointsExpression getFeaturePoints(GameState state, Player player) {
        return points;
    }

    @Override
    public PointsExpression getFeaturePoints() {
        return points;
    }
}
