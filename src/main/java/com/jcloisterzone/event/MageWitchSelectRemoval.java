package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class MageWitchSelectRemoval extends PlayEvent {

    public MageWitchSelectRemoval(Player triggeringPlayer, Player targetPlayer) {
        super(triggeringPlayer, targetPlayer);
    }

}
