package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class PlayerTurnEvent extends PlayEvent {

    public PlayerTurnEvent(Player targetPlayer) {
        super(null, targetPlayer);
    }

    @Override
    public String toString() {
        return super.toString() + " player:" + getTargetPlayer();
    }

}
