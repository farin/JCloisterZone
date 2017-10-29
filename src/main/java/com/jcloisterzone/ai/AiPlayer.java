package com.jcloisterzone.ai;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.SupportedSetup;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.Function1;
import io.vavr.collection.Vector;

public interface AiPlayer extends Function1<GameState, WsInGameMessage> {

    default SupportedSetup supportedSetup() {
        return SupportedSetup.getCurrentClientSupported();
    }

    default void onGameStart(GameSetup setup, Player me) {
    }

    default Vector<WsInGameMessage> getPossibleActions(GameState state) {
        ActionsState as = state.getPlayerActions();

        Vector<WsInGameMessage> messages = as.getActions().flatMap(action ->
            action.getOptions().map(o -> _This.createMessage(action, o)).toVector()
        );

        if (as.isPassAllowed()) {
            messages = messages.append(new PassMessage());
        }

        return messages;
    }

    // private helpers
    class _This {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static WsInGameMessage createMessage(PlayerAction action, Object option) {
            return action.select(option);
        }
    }
}
