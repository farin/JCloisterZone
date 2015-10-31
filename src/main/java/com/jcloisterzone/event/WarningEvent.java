package com.jcloisterzone.event;

import com.jcloisterzone.Player;

@Idempotent
public class WarningEvent extends PlayEvent {

    public WarningEvent(Player targetPlayer) {
        super(null, targetPlayer);
    }


}