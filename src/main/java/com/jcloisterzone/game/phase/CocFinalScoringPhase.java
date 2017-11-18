package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.game.RandomGenerator;

import io.vavr.collection.List;

public class CocFinalScoringPhase extends AbstractCocScoringPhase {

    public CocFinalScoringPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    protected List<Location> getScoredQuarters() {
        return List.of(Location.QUARTER_MARKET);
    }

}
