package com.jcloisterzone.game.phase;

import java.util.function.Function;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class CocFinalScoringPhase extends AbstractCocScoringPhase {

    public CocFinalScoringPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    protected  Map<Location, Function<Feature, Boolean>> getScoredQuarters(GameState state) {
        return HashMap.of(Location.QUARTER_MARKET, null);
    }

}
