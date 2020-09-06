package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.FinalScoring;


public class GameOverPhase extends Phase {

    public GameOverPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        state = state.setPlayerActions(null);
        state = state.mapPlayers(ps -> ps.setTurnPlayerIndex(null));
        state = (new FinalScoring()).apply(state);
        return promote(state);
    }
}
