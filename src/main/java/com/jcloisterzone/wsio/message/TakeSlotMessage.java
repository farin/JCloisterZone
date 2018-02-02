package com.jcloisterzone.wsio.message;

import com.jcloisterzone.game.SupportedSetup;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("TAKE_SLOT")
public class TakeSlotMessage extends AbstractWsMessage implements WsInGameMessage {

    private String gameId;
    private int number;
    private String nickname;
    private String aiClassName;
    private SupportedSetup supportedSetup;

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

    public SupportedSetup getSupportedSetup() {
        return supportedSetup;
    }

    public void setSupportedSetup(SupportedSetup supportedSetup) {
        this.supportedSetup = supportedSetup;
    }
}
