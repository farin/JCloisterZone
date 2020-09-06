package com.jcloisterzone.action;

import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.Set;

public class BazaarSelectTileAction extends AbstractPlayerAction<BazaarItem> {

    public BazaarSelectTileAction(Set<BazaarItem> options) {
        super(options);
    }

    @Override
    public Message select(BazaarItem target) {
        // server is invoked directly from BazaarPanel
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "select bazaar tile";
    }
}
