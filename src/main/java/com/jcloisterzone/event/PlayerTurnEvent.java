package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class PlayerTurnEvent extends Event {

    public PlayerTurnEvent(Player player) {
        super(player);
    }

}
