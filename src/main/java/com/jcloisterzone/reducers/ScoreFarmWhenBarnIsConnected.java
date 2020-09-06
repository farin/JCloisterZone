package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;

public class ScoreFarmWhenBarnIsConnected extends ScoreFarm {

    public ScoreFarmWhenBarnIsConnected(Farm feature) {
        super(feature, false);
    }

    @Override
    protected PointsExpression getFeaturePoints(GameState state, Player player) {
        Farm farm = getFeature();
        PointsExpression expr = farm.getPointsWhenBarnIsConnected(state, player);
        playerPoints = playerPoints.put(player, expr);
        return expr;
    }
}
