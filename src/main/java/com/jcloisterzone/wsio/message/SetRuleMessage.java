package com.jcloisterzone.wsio.message;

import com.jcloisterzone.game.Rule;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SET_RULE")
public class SetRuleMessage implements WsInGameMessage {

    private String gameId;
    private Rule rule;
    private Object value;

    public SetRuleMessage() {
    }

    public SetRuleMessage(Rule rule, Object value) {
        this.rule = rule;
        this.value = value;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
