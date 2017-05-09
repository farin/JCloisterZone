package com.jcloisterzone.game.state;

import java.util.function.Function;

import io.vavr.Function1;

public class MemloizedValue<T> implements Function1<GameState, T> {

    private final Function<GameState, T> fn;
    private T cachedValue;
    private GameState cachedState;

    public MemloizedValue(Function<GameState, T> fn) {
        this.fn = fn;
    }

    @Override
    public T apply(GameState state) {
        if (cachedState != state) {
            cachedValue = fn.apply(state);
            cachedState = state;
        }
        return cachedValue;
    }

}
