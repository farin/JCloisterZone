package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SLOT")
public class SlotMessage implements WsMessage {
    private String gameId;
    private int number;
    private Integer serial;
    private String nickname;
    private boolean ai;
    private SlotState state;
    private Expansion[] supportedExpansions;

    public SlotMessage(String gameId, int number, Integer serial, String nickname) {
        super();
        this.gameId = gameId;
        this.number = number;
        this.serial = serial;
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

    public boolean isAi() {
        return ai;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public SlotState getState() {
        return state;
    }

    public void setState(SlotState state) {
        this.state = state;
    }

    public Expansion[] getSupportedExpansions() {
        return supportedExpansions;
    }

    public void setSupportedExpansions(Expansion[] supportedExpansions) {
        this.supportedExpansions = supportedExpansions;
    }
}
