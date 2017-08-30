package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SET_EXPANSION")
public class SetExpansionMessage implements WsInGameMessage {

    private String gameId;
    private Expansion expansion;
    private boolean enabled;

    public SetExpansionMessage() {
    }

    public SetExpansionMessage(Expansion expansion, boolean enabled) {
        this.expansion = expansion;
        this.enabled = enabled;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
