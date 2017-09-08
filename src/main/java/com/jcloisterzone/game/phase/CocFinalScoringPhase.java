package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.config.Config;

import io.vavr.collection.List;

public class CocFinalScoringPhase extends AbstractCocScoringPhase {

    public CocFinalScoringPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    protected List<Location> getScoredQuarters() {
        return List.of(Location.QUARTER_MARKET);
    }

}
