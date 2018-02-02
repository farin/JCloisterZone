package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SET_EXPANSION")
public class SetExpansionMessage extends AbstractWsMessage implements WsInGameMessage {

    private String gameId;
    private Expansion expansion;
    private int count;

    public SetExpansionMessage() {
    }

    public SetExpansionMessage(Expansion expansion, int count) {
        this.expansion = expansion;
        this.count = count;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Expansion getExpansion() {
        return expansion;
    }

    public void setExpansion(Expansion expansion) {
        this.expansion = expansion;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
