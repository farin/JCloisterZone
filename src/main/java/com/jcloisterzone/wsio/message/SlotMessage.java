package com.jcloisterzone.wsio.message;

import com.jcloisterzone.game.SupportedSetup;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SLOT")
public class SlotMessage implements WsInGameMessage {

    private String gameId;
    private int number;
    private String sessionId;
    private String clientId;
    private Integer serial;
    private String nickname;
    private String aiClassName;
    private SupportedSetup supportedSetup;

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

    public SupportedSetup getSupportedSetup() {
        return supportedSetup;
    }

    public void setSupportedSetup(SupportedSetup supportedSetup) {
        this.supportedSetup = supportedSetup;
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
