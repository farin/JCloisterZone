package com.jcloisterzone.action;

import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.BazaarPanel;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

@LinkedPanel(BazaarPanel.class)
public class BazaarSelectTileAction extends PlayerAction<BazaarItem> {

    public BazaarSelectTileAction(Set<BazaarItem> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(BazaarItem target) {
        // server is invoked directly from BazaarPanel
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "select bazaar tile";
    }
}
