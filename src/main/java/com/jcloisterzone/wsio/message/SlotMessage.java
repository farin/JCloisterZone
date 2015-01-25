package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SLOT")
public class SlotMessage implements WsInGameMessage {
    private String gameId;
    private int number;
    private String owner;
    private Integer serial;
    private String nickname;
    private String aiClassName;
    private Expansion[] supportedExpansions;

    public SlotMessage(String gameId, int number, Integer serial, String owner, String nickname) {
        super();
        this.gameId = gameId;
        this.number = number;
        this.serial = serial;
        this.owner = owner;
        this.nickname = nickname;
    }

    public String getGameId() {
        return gameId;
    }

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

    public Expansion[] getSupportedExpansions() {
        return supportedExpansions;
    }

    public void setSupportedExpansions(Expansion[] supportedExpansions) {
        this.supportedExpansions = supportedExpansions;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
