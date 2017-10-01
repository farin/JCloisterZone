package com.jcloisterzone.ai;

import com.jcloisterzone.game.state.GameState;

import io.vavr.Function1;

public interface GameStateRanking extends Function1<GameState, Double> {


}
