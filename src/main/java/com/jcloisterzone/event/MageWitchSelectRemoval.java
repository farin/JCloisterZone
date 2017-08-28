package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent;

public class MageWitchSelectRemoval extends PlayEvent {

    public MageWitchSelectRemoval(Player triggeringPlayer, Player targetPlayer) {
        super(triggeringPlayer, targetPlayer);
    }

}
