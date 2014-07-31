package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.wsio.Cmd;

@Cmd("SET_EXPANSION")
public class SetExpansionMessage implements WsMessage {
    private String gameId;
    private Expansion expansion;
    private boolean enabled;

    public SetExpansionMessage(String gameId, Expansion expansion, boolean enabled) {
        this.gameId = gameId;
        this.expansion = expansion;
        this.enabled = enabled;
    }

    public String getGameId() {
        return gameId;
    }

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
