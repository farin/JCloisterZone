package com.jcloisterzone.ai.player;

import java.util.Random;

import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Vector;

public class DummyAiPlayer implements AiPlayer {

    private Random random = new Random();

    @Override
    public WsInGameMessage apply(GameState state) {
        Vector<WsInGameMessage> messages = getPossibleActions(state);
        return messages.get(random.nextInt(messages.length()));
    }
}
