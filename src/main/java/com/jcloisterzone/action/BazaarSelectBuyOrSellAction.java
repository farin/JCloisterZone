package com.jcloisterzone.action;

import com.jcloisterzone.io.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.io.message.BazaarBuyOrSellMessage.BuyOrSellOption;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import java.util.Arrays;


public class BazaarSelectBuyOrSellAction extends AbstractPlayerAction<Void>{

    public BazaarSelectBuyOrSellAction() {
        super(null);
    }

}
