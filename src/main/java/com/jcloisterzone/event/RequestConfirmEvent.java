package com.jcloisterzone.event;

import com.jcloisterzone.Player;

@Idempotent
public class RequestConfirmEvent extends PlayEvent {

    public RequestConfirmEvent(Player targetPlayer) {
        super(null, targetPlayer);
    }


}
