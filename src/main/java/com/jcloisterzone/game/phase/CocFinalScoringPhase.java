package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.board.Location;

import io.vavr.collection.List;

public class CocFinalScoringPhase extends AbstractCocScoringPhase {

    public CocFinalScoringPhase(Random random) {
        super(random);
    }

    @Override
    protected List<Location> getScoredQuarters() {
        return List.of(Location.QUARTER_MARKET);
    }

}
