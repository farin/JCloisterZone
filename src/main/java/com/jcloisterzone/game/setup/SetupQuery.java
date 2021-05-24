package com.jcloisterzone.game.setup;

import com.jcloisterzone.engine.Game;
import com.jcloisterzone.game.GameSetup;
import io.vavr.Function1;

import java.util.function.Function;

public interface SetupQuery extends Function1<GameSetup, Boolean> {
}
