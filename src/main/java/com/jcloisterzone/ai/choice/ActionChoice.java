package com.jcloisterzone.ai.choice;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ai.GameRanking;
import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.wsio.RmiProxy;

public class ActionChoice<T> extends AiChoice{

    private final PlayerAction<T> action;
    private final T value;

    public ActionChoice(AiChoice previous, SavePoint savePoint, PlayerAction<T> action, T value) {
        super(previous, savePoint);
        this.action = action;
        this.value = value;
    }

    public void perform(RmiProxy server) {
        action.perform(server, value);
    }

    public void rankPartial(GameRanking gr, Game game) {
        //empty default
    }

    public PlayerAction<T> getAction() {
        return action;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return (new StringBuilder(action.toString())).append(" -> ").append(value.toString()).toString();
    }


}
