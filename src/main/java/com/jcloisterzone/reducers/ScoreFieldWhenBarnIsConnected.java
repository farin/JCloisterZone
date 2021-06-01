package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.game.state.GameState;

public class ScoreFieldWhenBarnIsConnected extends ScoreField {

    public ScoreFieldWhenBarnIsConnected(Field feature) {
        super(feature, false);
    }

    @Override
    protected PointsExpression getFeaturePoints(GameState state, Player player) {
        Field field = getFeature();
        PointsExpression expr = field.getPointsWhenBarnIsConnected(state, player);
        playerPoints = playerPoints.put(player, expr);
        return expr;
    }
}
