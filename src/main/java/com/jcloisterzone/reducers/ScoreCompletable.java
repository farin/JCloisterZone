package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;

public class ScoreCompletable extends ScoreFeature implements ScoreFeatureReducer {

    // points is store to instance and can be accesed after reduce
    private int points;

    public ScoreCompletable(Completable feature) {
        super(feature);
    }

    @Override
    protected int getFeaturePoints(GameState state, Player player) {
        return points;
    }

    @Override
    public int getFeaturePoints() {
    		return points;
    }

    @Override
    public Completable getFeature() {
        return (Completable) super.getFeature();
    }

    @Override
    public GameState apply(GameState state) {
        points = getFeature().getPoints(state);
        return super.apply(state);
    }
}
