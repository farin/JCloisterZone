package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.capability.CountCapability;

import io.vavr.collection.List;

@RequiredCapability(CountCapability.class)
public class CocScoringPhase extends AbstractCocScoringPhase {

    public CocScoringPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    protected List<Location> getScoredQuarters() {
        return List.of(Location.QUARTER_BLACKSMITH, Location.QUARTER_CASTLE, Location.QUARTER_CATHEDRAL);
    }

}
