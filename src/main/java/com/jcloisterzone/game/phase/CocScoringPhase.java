package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.CountCapability;

import io.vavr.collection.List;

@RequiredCapability(CountCapability.class)
public class CocScoringPhase extends AbstractCocScoringPhase {

    public CocScoringPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    protected List<Location> getScoredQuarters() {
        return List.of(Location.QUARTER_BLACKSMITH, Location.QUARTER_CASTLE, Location.QUARTER_CATHEDRAL);
    }

}
