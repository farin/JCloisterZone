package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.capability.BlackDragonCapability;
import com.jcloisterzone.game.capability.BlackDragonCapabilityModel;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;

public class BlackDragonPhase extends Phase {

    private BlackDragonMovePhase blackDragonMovePhase;

    public BlackDragonPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
        blackDragonMovePhase = new BlackDragonMovePhase(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        BlackDragonCapabilityModel model = state.getCapabilityModel(BlackDragonCapability.class);
        if (model.getMoves() > 0) {
            return next(state, blackDragonMovePhase);
        }
        return next(state);
    }
}
