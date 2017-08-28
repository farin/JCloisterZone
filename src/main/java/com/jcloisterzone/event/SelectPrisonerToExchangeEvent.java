package com.jcloisterzone.event;

import com.jcloisterzone.Player;

@Idempotent
public class SelectPrisonerToExchangeEvent extends PlayEvent {
    public SelectPrisonerToExchangeEvent(Player player) {
        super(null, player);
    }
}