package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("PAY_RANSOM")
public class PayRansomMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String meepleId;

    public PayRansomMessage(String gameId, String meepleId) {
        super();
        this.gameId = gameId;
        this.meepleId = meepleId;
    }

    public String getGameId() {
        return gameId;
    }

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