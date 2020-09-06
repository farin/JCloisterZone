package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("BAZAAR_BUY_OR_SELL")
public class BazaarBuyOrSellMessage extends AbstractMessage implements ReplayableMessage {

    public enum BuyOrSellOption { BUY, SELL }

    private BuyOrSellOption value;

    public BazaarBuyOrSellMessage() {
    }

    public BazaarBuyOrSellMessage(BuyOrSellOption value) {
        this.value = value;
    }


    public BuyOrSellOption getValue() {
        return value;
    }

    public void setValue(BuyOrSellOption value) {
        this.value = value;
    }


}
