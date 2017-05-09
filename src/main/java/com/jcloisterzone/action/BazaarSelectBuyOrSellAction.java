package com.jcloisterzone.action;

import java.util.Arrays;

import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.BazaarPanel;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage.BuyOrSellOption;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

@LinkedPanel(BazaarPanel.class)
public class BazaarSelectBuyOrSellAction extends PlayerAction<BuyOrSellOption>{

    public BazaarSelectBuyOrSellAction() {
        this(HashSet.ofAll(Arrays.asList(BuyOrSellOption.values())));
    }

    public BazaarSelectBuyOrSellAction(Set<BuyOrSellOption> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, BuyOrSellOption option) {
        gc.getConnection().send(
            new BazaarBuyOrSellMessage(gc.getGameId(), option)
        );
    }

    @Override
    public String toString() {
        return "BUY or SELL";
    }

}
