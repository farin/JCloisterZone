package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class PlayerTurnEvent extends PlayEvent {

    public PlayerTurnEvent(Player targetPlayer) {
        this(null, targetPlayer);
    }
    
    public PlayerTurnEvent(Player triggeringPlayer, Player targetPlayer) {
        super(triggeringPlayer, targetPlayer);
    }

    @Override
    public String toString() {
        return super.toString() + " player:" + getTargetPlayer();
    }

}
