package com.jcloisterzone.wsio.message;

import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.adapters.CapabilitiesSetAdapter;

@WsMessageCommand("SLOT")
public class SlotMessage implements WsInGameMessage {

    private String gameId;
    private int number;
    private String sessionId;
    private String clientId;
    private Integer serial;
    private String nickname;
    private String aiClassName;
    @JsonAdapter(CapabilitiesSetAdapter.class)
    Set<Class<? extends Capability<?>>> supportedCapabilities;

    public SlotMessage(int number, Integer serial, String sessionId, String clientId, String nickname) {
        this.number = number;
        this.serial = serial;
        this.sessionId = sessionId;
        this.clientId = clientId;
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

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public Set<Class<? extends Capability<?>>> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    public void setSupportedCapabilities(Set<Class<? extends Capability<?>>> supportedCapabilities) {
        this.supportedCapabilities = supportedCapabilities;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
