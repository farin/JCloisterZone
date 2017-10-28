package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class ScoreFarm extends ScoreFeature {

    protected Map<Player, Integer> playerPoints = HashMap.empty();

    public ScoreFarm(Farm feature) {
        super(feature);
    }

    @Override
    protected int getFeaturePoints(GameState state, Player player) {
        int value = getFeature().getPoints(state, player);
        playerPoints = playerPoints.put(player, value);
        return value;
    }

    @Override
    public int getFeaturePoints() {
        throw new UnsupportedOperationException("Call getFeaturePoints() with player argument");
    }

    @Override
    public int getFeaturePoints(Player player) {
        return playerPoints.get(player).getOrElse(0);
    }

    @Override
    public Farm getFeature() {
        return (Farm) super.getFeature();
    }

}
