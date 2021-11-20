package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.capability.BigTopCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;

public class BigTopMovePhase extends Phase {

    public BigTopMovePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
		BigTopCapability capability = state.getCapabilities().get(BigTopCapability.class);
		state = capability.bigTopMove(state);
		return next(state);
    }
}
