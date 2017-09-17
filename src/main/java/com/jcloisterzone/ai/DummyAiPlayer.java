package com.jcloisterzone.ai;

import java.util.Random;

import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Vector;

public class DummyAiPlayer implements AiPlayer {

    private Random random = new Random();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public WsInGameMessage apply(GameState state) {
        ActionsState as = state.getPlayerActions();

        Vector<PlayerChoice> choices = as.getActions().flatMap(action ->
            action.getOptions().map(o -> new PlayerChoice(action, o)).toVector()
        );

        if (as.isPassAllowed()) {
            choices = choices.append(new PlayerChoice(null, null));
        }

        PlayerChoice choice = choices.get(random.nextInt(choices.length()));
        return choice.getWsMessage();
    }

}
