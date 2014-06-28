package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.capability.BazaarItem;

@Idempotent
public class BazaarSelectBuyOrSellEvent extends PlayEvent {

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
