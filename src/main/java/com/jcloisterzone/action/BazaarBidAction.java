package com.jcloisterzone.action;

import com.jcloisterzone.io.message.Message;

public class BazaarBidAction extends AbstractPlayerAction<Void> {

    public BazaarBidAction() {
        super(null);
    }

    @Override
    public Message select(Void target) {
        // server is invoked directly from BazaarPanel
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "make bazaar bid";
    }

}
