package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("BAZAAR_BID")
public class BazaarBidMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    int supplyIndex;
    int price;

    public BazaarBidMessage(String gameId, int supplyIndex, int price) {
        super();
        this.gameId = gameId;
        this.supplyIndex = supplyIndex;
        this.price = price;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
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
