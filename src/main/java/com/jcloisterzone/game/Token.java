package com.jcloisterzone.game;

import io.vavr.collection.List;

public enum Token {

    WINE, GRAIN, CLOTH,
    KING, ROBBER,
    LB_SHED, LB_HOUSE, LB_TOWER,
    TOWER_PIECE,
    ABBEY_TILE,
    BRIDGE,
    CASTLE,
    FERRY,
    GOLD,
    TUNNEL_A, TUNNEL_B, TUNNEL_C;

    public boolean isTunnel() {
        return this == TUNNEL_A || this == TUNNEL_B || this == TUNNEL_C;
    }

    public static Token[] tunnelValues() {
        return new Token[] {Token.TUNNEL_A, Token.TUNNEL_B, Token.TUNNEL_C};
    }
}
