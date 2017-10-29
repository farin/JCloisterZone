package com.jcloisterzone.wsio.message;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.adapters.CapabilitiesAdapter;

@WsMessageCommand("SET_CAPABILITY")
public class SetCapabilityMessage implements WsInGameMessage {

    private String gameId;
    @JsonAdapter(CapabilitiesAdapter.class)
    private Class<? extends Capability<?>> capability;
    private boolean enabled;

    public SetCapabilityMessage() {
    }

    public SetCapabilityMessage(Class<? extends Capability<?>> capability, boolean enabled) {
        this.capability = capability;
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

    public Class<? extends Capability<?>> getCapability() {
        return capability;
    }

    public void setCapability(Class<? extends Capability<?>> capability) {
        this.capability = capability;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
