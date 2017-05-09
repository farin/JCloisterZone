package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.state.GameState;

public class StepResult {

    private final GameState state;
    private final Class<? extends Phase> next;

    public StepResult(GameState state, Class<? extends Phase> next) {
        super();
        this.state = state;
        this.next = next;
    }

    public GameState getState() {
        return state;
    }

    public Class<? extends Phase> getNext() {
        return next;
    }
}
