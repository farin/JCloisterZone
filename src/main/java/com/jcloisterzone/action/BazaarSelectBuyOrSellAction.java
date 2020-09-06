package com.jcloisterzone.action;

import com.jcloisterzone.io.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.io.message.BazaarBuyOrSellMessage.BuyOrSellOption;
import com.jcloisterzone.io.message.Message;
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
    public Message select(BuyOrSellOption option) {
        return new BazaarBuyOrSellMessage(option);
    }

    @Override
    public String toString() {
        return "BUY or SELL";
    }

}
