package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class PlayerTurnEvent extends PlayEvent {

    public PlayerTurnEvent(Player player) {
        super(player);
    }

    @Override
    public String toString() {
        return super.toString() + " player:" + getPlayer();
    }

}
