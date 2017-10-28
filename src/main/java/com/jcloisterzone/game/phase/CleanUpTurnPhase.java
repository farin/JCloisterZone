package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.SetNextPlayer;

import io.vavr.collection.HashSet;

/**
 * real end of turn and switch to next player
 */
public class CleanUpTurnPhase extends Phase {

    public CleanUpTurnPhase(Random random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onTurnCleanUp(state);
        }

        if (!state.getFlags().isEmpty()) {
            state = state.setFlags(HashSet.empty());
        }
        state = (new SetNextPlayer()).apply(state);

        return next(state);
    }
}
