package com.jcloisterzone.game.state;

import io.vavr.Function1;

import java.util.function.Function;

public class MemoizedValue<T> implements Function1<GameState, T> {

    private final Function<GameState, T> fn;
    private T cachedValue;
    private GameState cachedState;

    public MemoizedValue(Function<GameState, T> fn) {
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
