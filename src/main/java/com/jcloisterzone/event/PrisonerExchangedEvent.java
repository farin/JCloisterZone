package com.jcloisterzone.event;

import com.jcloisterzone.Player;

@Idempotent
public class PrisonerExchangedEvent extends PlayEvent {
    public PrisonerExchangedEvent(Player player) {
        super(null, player);
    }
}