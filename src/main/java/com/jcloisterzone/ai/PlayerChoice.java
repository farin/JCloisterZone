package com.jcloisterzone.ai;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

public class PlayerChoice<T> {

    private final PlayerAction<T> action;
    private final T option;

    public PlayerChoice(PlayerAction<T> action, T option) {
        this.action = action;
        this.option = option;
    }

    public PlayerAction<T> getAction() {
        return action;
    }

    public T getOption() {
        return option;
    }

    public WsInGameMessage getWsMessage() {
        if (action == null) {
            return new PassMessage();
        } else {
            return action.select(option);
        }
    }

    @Override
    public String toString() {
        return String.format("%s/%s", action, option);
    }
}
