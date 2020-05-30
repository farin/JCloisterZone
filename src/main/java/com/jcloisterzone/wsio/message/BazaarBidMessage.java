package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("BAZAAR_BID")
public class BazaarBidMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private long clock;
    private String parentId;
    private int supplyIndex;
    private int price;

    public BazaarBidMessage() {
    }

    public BazaarBidMessage(int supplyIndex, int price) {
        this.supplyIndex = supplyIndex;
        this.price = price;
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

    public int getSupplyIndex() {
        return supplyIndex;
    }

    public void setSupplyIndex(int supplyIndex) {
        this.supplyIndex = supplyIndex;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
