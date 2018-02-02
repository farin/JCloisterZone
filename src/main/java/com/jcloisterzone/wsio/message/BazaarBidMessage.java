package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("BAZAAR_BID")
public class BazaarBidMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String messageId;
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
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
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
