package com.jcloisterzone.ai;

import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.Function1;

public interface AiPlayer extends Function1<GameState, WsInGameMessage> {

}
