package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.FinalScoring;
import com.jcloisterzone.wsio.message.GameOverMessage;


public class GameOverPhase extends Phase {

    public GameOverPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        state = state.setPlayerActions(null);
        state = (new FinalScoring()).apply(state);
        return promote(state);
    }

    @PhaseMessageHandler
    public StepResult handleGameOverMessage(GameState state, GameOverMessage msg) {
        // do nothing, message is just recorded to replay
        return new StepResult(state, null);
    }

}
