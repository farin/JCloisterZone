package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;

public class ScoreFarmWhenBarnIsConnected extends ScoreFeature {

    public ScoreFarmWhenBarnIsConnected(Farm feature) {
        super(feature);
    }

    @Override
    protected int getFeaturePoints(GameState state, Player player) {
        Farm farm = getFeature();
        return farm.getPointsWhenBarnIsConnected(state, player);
    }

    @Override
    public Farm getFeature() {
        return (Farm) super.getFeature();
    }
}
