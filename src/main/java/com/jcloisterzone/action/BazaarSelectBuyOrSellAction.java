package com.jcloisterzone.action;

import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage.BuyOrSellOption;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import java.util.Arrays;

public class BazaarSelectBuyOrSellAction extends AbstractPlayerAction<BuyOrSellOption>{

    public BazaarSelectBuyOrSellAction() {
        this(HashSet.ofAll(Arrays.asList(BuyOrSellOption.values())));
    }

    public BazaarSelectBuyOrSellAction(Set<BuyOrSellOption> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(BuyOrSellOption option) {
        return new BazaarBuyOrSellMessage(option);
    }

    @Override
    public String toString() {
        return "BUY or SELL";
    }

}
