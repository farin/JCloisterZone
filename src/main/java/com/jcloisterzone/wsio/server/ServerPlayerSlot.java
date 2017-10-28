package com.jcloisterzone.wsio.server;

import java.util.Set;

import com.jcloisterzone.game.Capability;

//TODO use slot message directly
public class ServerPlayerSlot {

    private final int number;
    private Integer serial; // server assign sequence number when type is occupied
    private String nickname;
    private String sessionId;
    private String aiClassName;
    private Set<Class<? extends Capability<?>>> supportedCapabilities;

    private String clientId;
    private String autoAssignClientId;
    private String secret;

    public ServerPlayerSlot(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return sessionId != null;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAiClassName() {
        return aiClassName;
    }

    public void setAiClassName(String aiClassName) {
        this.aiClassName = aiClassName;
    }

    public int getNumber() {
        return number;
    }

    public Set<Class<? extends Capability<?>>> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    public void setSupportedCapabilities(Set<Class<? extends Capability<?>>> supportedCapabilities) {
        this.supportedCapabilities = supportedCapabilities;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAutoAssignClientId() {
        return autoAssignClientId;
    }

    public void setAutoAssignClientId(String autoAssignClientId) {
        this.autoAssignClientId = autoAssignClientId;
    }
}
