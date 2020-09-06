package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("BAZAAR_BID")
public class BazaarBidMessage extends AbstractMessage implements ReplayableMessage {

    private int supplyIndex;
    private int price;

    public BazaarBidMessage() {
    }

    public BazaarBidMessage(int supplyIndex, int price) {
        this.supplyIndex = supplyIndex;
        this.price = price;
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
