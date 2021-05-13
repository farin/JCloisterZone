package com.jcloisterzone.game.phase;

import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.FinalScoring;


public class GameOverPhase extends Phase {

    public GameOverPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        state = state.setPlayerActions(null);
        state = state.mapPlayers(ps -> ps.setTurnPlayerIndex(null));
        state = (new FinalScoring()).apply(state);
        return promote(state);
    }
}
