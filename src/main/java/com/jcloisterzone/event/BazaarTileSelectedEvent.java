package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.capability.BazaarItem;

public class BazaarTileSelectedEvent extends PlayEvent {

    private final BazaarItem bazaarItem;
    private final int supplyIndex;

    public BazaarTileSelectedEvent(Player targetPlayer, BazaarItem bazaarItem, int supplyIndex) {
        super(null, targetPlayer);
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
