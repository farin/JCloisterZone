package com.jcloisterzone.wsio.message;

import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.adapters.CapabilitiesSetAdapter;

@WsMessageCommand("TAKE_SLOT")
public class TakeSlotMessage implements WsInGameMessage {

    private String gameId;
    private int number;
    private String nickname;
    private String aiClassName;
    @JsonAdapter(CapabilitiesSetAdapter.class)
    private Set<Class<? extends Capability<?>>> supportedCapabilities;

    public TakeSlotMessage() {
    }

    public TakeSlotMessage(int number, String nickname) {
        this.number = number;
        this.nickname = nickname;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public String getAiClassName() {
        return aiClassName;
    }

    public void setAiClassName(String aiClassName) {
        this.aiClassName = aiClassName;
    }

    public Set<Class<? extends Capability<?>>> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    public void setSupportedCapabilities(Set<Class<? extends Capability<?>>> supportedCapabilities) {
        this.supportedCapabilities = supportedCapabilities;
    }
}
