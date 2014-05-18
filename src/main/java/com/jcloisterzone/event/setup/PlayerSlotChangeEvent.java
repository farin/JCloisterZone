package com.jcloisterzone.event.setup;

import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.PlayerSlot;

public class PlayerSlotChangeEvent extends Event {

    private final PlayerSlot slot;

    public PlayerSlotChangeEvent(PlayerSlot slot) {
        super();
        this.slot = slot;
    }

    public PlayerSlot getSlot() {
        return slot;
    }

}
