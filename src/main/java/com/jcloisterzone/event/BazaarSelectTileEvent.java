package com.jcloisterzone.event;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.capability.BazaarItem;

@Idempotent
public class BazaarSelectTileEvent extends PlayEvent {

    private List<BazaarItem> bazaarSupply;

    public BazaarSelectTileEvent(Player targetPlayer, List<BazaarItem> bazaarSupply) {
        super(null, targetPlayer);
        this.bazaarSupply = bazaarSupply;
    }

    public List<BazaarItem> getBazaarSupply() {
        return bazaarSupply;
    }

}
