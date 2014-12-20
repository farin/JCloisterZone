package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;

public class TradeResourceEvent extends PlayEvent implements Undoable {

    private final TradeResource resource;
    private final int count;

    public TradeResourceEvent(Player player, TradeResource resource, int count) {
        super(player, player);
        this.resource = resource;
        this.count = count;
    }

    public TradeResource getResource() {
        return resource;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void undo(Game game) {
        ClothWineGrainCapability cap = game.getCapability(ClothWineGrainCapability.class);
        cap.addTradeResources(getTargetPlayer(), resource, -count);
    }
}
