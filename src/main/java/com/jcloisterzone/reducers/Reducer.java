package com.jcloisterzone.reducers;

import com.jcloisterzone.game.state.GameState;
import io.vavr.Function1;

public interface Reducer extends Function1<GameState, GameState> {

}
