package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class PlayerTurnEvent extends PlayEvent {

    public PlayerTurnEvent(Player player) {
        super(player);
    }

}
