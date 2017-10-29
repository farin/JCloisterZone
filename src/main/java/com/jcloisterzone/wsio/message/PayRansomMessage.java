package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("PAY_RANSOM")
public class PayRansomMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String meepleId;

    public PayRansomMessage() {
    }

    public PayRansomMessage(String meepleId) {
        super();
        this.meepleId = meepleId;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }
}