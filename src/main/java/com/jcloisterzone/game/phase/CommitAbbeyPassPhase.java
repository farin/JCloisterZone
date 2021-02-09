package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.AbbeyCapability;

@RequiredCapability(AbbeyCapability.class)
public class CommitAbbeyPassPhase extends CommitActionPhase {

    public CommitAbbeyPassPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

}
