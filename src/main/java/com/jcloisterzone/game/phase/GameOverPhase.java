package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.FinalScoring;
import com.jcloisterzone.ui.GameController;


public class GameOverPhase extends Phase {

    public GameOverPhase(GameController gc) {
        super(gc);
    }

    @Override
    public StepResult enter(GameState state) {
        state = state.setPlayerActions(null);
        state = (new FinalScoring()).apply(state);
        return promote(state);
    }
}
