package com.jcloisterzone.game.phase;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class StepResult {

    private final GameState state;
    private final Phase next;

    public StepResult(GameState state, Phase next) {
        super();
        this.state = state;
        this.next = next;
    }

    public GameState getState() {
        return state;
    }

    public Phase getNext() {
        return next;
    }
}
