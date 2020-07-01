package com.jcloisterzone.action;

import com.jcloisterzone.wsio.message.WsInGameMessage;

public class BazaarBidAction extends AbstractPlayerAction<Void> {

    public BazaarBidAction() {
        super(null);
    }

    @Override
    public WsInGameMessage select(Void target) {
        // server is invoked directly from BazaarPanel
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "make bazaar bid";
    }

}
