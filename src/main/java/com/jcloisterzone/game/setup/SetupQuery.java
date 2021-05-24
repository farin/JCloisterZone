package com.jcloisterzone.game.setup;

import com.jcloisterzone.engine.Game;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Function1;

import java.util.function.Function;

public interface SetupQuery extends Function1<GameState, Boolean> {
}
