package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;

public class ScoreFarmWhenBarnIsConnected extends ScoreFarm {


    public ScoreFarmWhenBarnIsConnected(Farm feature) {
        super(feature);
    }

    @Override
    protected int getFeaturePoints(GameState state, Player player) {
        Farm farm = getFeature();
        int points = farm.getPointsWhenBarnIsConnected(state, player);
        playerPoints = playerPoints.put(player, points);
        return points;
    }
}
