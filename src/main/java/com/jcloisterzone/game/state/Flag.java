package com.jcloisterzone.game.state;

public enum Flag {
    // Cleared at the turn end
    RANSOM_PAID, BAZAAR_AUCTION, TUNNEL_PLACED,

    // Cleared at the turn part end
    PORTAL_USED, PRINCESS_USED, FLYING_MACHINE_USED,
    // Fan Expansions
    ROBBERSSON_USED
}