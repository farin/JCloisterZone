package com.jcloisterzone.ai;

import java.util.Random;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Vector;

public class DummyAiPlayer implements AiPlayer {

    private Random random = new Random();




    @Override
    public WsInGameMessage apply(GameState state) {
        ActionsState as = state.getPlayerActions();

        Vector<WsInGameMessage> messages = as.getActions().flatMap(action ->
            action.getOptions().map(o -> createMessage(action, o)).toVector()
        );

        if (as.isPassAllowed()) {
            messages = messages.append(new PassMessage());
        }

        return messages.get(random.nextInt(messages.length()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private WsInGameMessage createMessage(PlayerAction action, Object option) {
        return action.select(option);
    }

}
