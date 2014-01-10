package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.capability.BazaarItem;

public class BazaarSelectBuyOrSellEvent extends Event {

    private final BazaarItem bazaarItem;
    private final int supplyIndex;

    public BazaarSelectBuyOrSellEvent(Player player, BazaarItem bazaarItem, int supplyIndex) {
        super(player);
        this.bazaarItem = bazaarItem;
        this.supplyIndex = supplyIndex;
    }

    public BazaarItem getBazaarItem() {
        return bazaarItem;
    }

    public int getSupplyIndex() {
        return supplyIndex;
    }
}
