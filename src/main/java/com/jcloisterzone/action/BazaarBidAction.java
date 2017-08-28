package com.jcloisterzone.action;

import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.BazaarPanel;

@LinkedPanel(BazaarPanel.class)
public class BazaarBidAction extends PlayerAction<Void> {

    public BazaarBidAction() {
        super(null);
    }

    @Override
    public void perform(GameController gc, Void target) {
        // server is invoked directly from BazaarPanel
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "make bazaar bid";
    }

}
