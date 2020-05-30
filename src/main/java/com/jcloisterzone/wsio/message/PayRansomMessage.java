package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("PAY_RANSOM")
public class PayRansomMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private long clock;
    private String parentId;
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

    @Override
    public long getClock() {
        return clock;
    }

    @Override
    public void setClock(long clock) {
        this.clock = clock;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }
}