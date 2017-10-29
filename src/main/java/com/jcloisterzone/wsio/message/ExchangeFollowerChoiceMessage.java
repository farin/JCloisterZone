package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("EXCHANGE_FOLLOWER")
public class ExchangeFollowerChoiceMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String meepleId;

    public ExchangeFollowerChoiceMessage() {
    }

    public ExchangeFollowerChoiceMessage(String meepleId) {
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
