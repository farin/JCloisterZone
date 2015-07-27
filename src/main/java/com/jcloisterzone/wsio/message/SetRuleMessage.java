package com.jcloisterzone.wsio.message;

import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SET_RULE")
public class SetRuleMessage implements WsInGameMessage {
    private String gameId;
    private CustomRule rule;
    private Object value;

    public SetRuleMessage(String gameId, CustomRule rule, Object value) {
        this.gameId = gameId;
        this.rule = rule;
        this.value = value;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public CustomRule getRule() {
        return rule;
    }

    public void setRule(CustomRule rule) {
        this.rule = rule;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
