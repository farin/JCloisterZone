package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;

public class ScoreCompletable extends ScoreFeature implements ScoreFeatureReducer {

    // points is store to instance and can be accessed after reduce
    private PointsExpression points;

    public ScoreCompletable(Completable feature, boolean isFinal) {
        super(feature, isFinal);
    }

    @Override
    protected PointsExpression getFeaturePoints(GameState state, Player player) {
        return points;
    }

    @Override
    public PointsExpression getFeaturePoints() {
        return points;
    }

    @Override
    public Completable getFeature() {
        return (Completable) super.getFeature();
    }

    @Override
    public GameState apply(GameState state) {
        points = getFeature().getPoints(state);
        state = super.apply(state);

        if (!isFinal) {
            Mage mage = state.getNeutralFigures().getMage();
            if (mage != null && mage.getFeature(state) == getFeature()) {
                state = (new ReturnNeutralFigure(mage)).apply(state);
            }

            Witch witch = state.getNeutralFigures().getWitch();
            if (witch != null && witch.getFeature(state) == getFeature()) {
                state = (new ReturnNeutralFigure(witch)).apply(state);
            }
        }

        return state;
    }
}
