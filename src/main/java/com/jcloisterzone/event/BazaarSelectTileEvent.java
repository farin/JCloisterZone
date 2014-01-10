package com.jcloisterzone.event;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.capability.BazaarItem;

public class BazaarSelectTileEvent extends Event {

    private List<BazaarItem> bazaarSupply;

    public BazaarSelectTileEvent(Player player, List<BazaarItem> bazaarSupply) {
        super(player);
        this.bazaarSupply = bazaarSupply;
    }

    public List<BazaarItem> getBazaarSupply() {
        return bazaarSupply;
    }

}
