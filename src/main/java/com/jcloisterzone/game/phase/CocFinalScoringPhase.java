package com.jcloisterzone.game.phase;

import java.util.function.Function;

import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.state.GameState;

public class CocFinalScoringPhase extends AbstractCocScoringPhase {

    public CocFinalScoringPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    protected Function<Feature, Boolean> getAllowedFeaturesFilter(GameState state) {
        return f -> {
            if (f instanceof Farm) {
                return true;
            }
            if (f instanceof Completable) {
                return !((Completable)f).isCompleted(state);
            }
            throw new UnsupportedOperationException();
        };
    }


}
