package com.jcloisterzone.feature;

import com.jcloisterzone.Player;

public class TunnelEnd {

    public static enum TunnelToken {
        A, B
    }

    private final Player occupiedBy;
    private final TunnelToken token;

    public TunnelEnd() {
        this(null, null);
    }

    public TunnelEnd(Player occupiedBy, TunnelToken token) {
        this.occupiedBy = occupiedBy;
        this.token = token;
    }

    public Player getOccupiedBy() {
        return occupiedBy;
    }

    public TunnelToken getToken() {
        return token;
    }
}
