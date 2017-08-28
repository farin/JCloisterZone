package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;

public class ScoreFarm extends ScoreFeature {

    public ScoreFarm(Farm feature) {
        super(feature);
    }

    @Override
    int getFeaturePoints(GameState state, Player player) {
        return getFeature().getPoints(state, player);
    }

    @Override
    public Farm getFeature() {
        return (Farm) super.getFeature();
    }

}
