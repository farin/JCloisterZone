package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Expansion;

public class SetExpansionMessage {
    String gameId;
    Expansion expansion;
    boolean enabled;

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
