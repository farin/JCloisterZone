package com.jcloisterzone.game;

public enum Token {

    WINE, GRAIN, CLOTH,
    KING, ROBBER, // awarded bonuses
    LB_SHED, LB_HOUSE, LB_TOWER,
    TOWER_PIECE,
    ABBEY_TILE, // represent Abbey tile in player's supply
    BRIDGE,
    CASTLE,
    FERRY,
    GOLD,
    TUNNEL_A, TUNNEL_B, TUNNEL_C;

    public boolean isTunnel() {
        return this == TUNNEL_A || this == TUNNEL_B || this == TUNNEL_C;
    }

    public static Token[] tunnelValues() {
        return new Token[] {TUNNEL_A, TUNNEL_B, TUNNEL_C};
    }

    public boolean isLittleBuilding() {
        return this == LB_SHED || this == LB_HOUSE || this == LB_TOWER;
    }

    public static Token[] littleBuildingValues() {
        return new Token[] {LB_SHED, LB_HOUSE, LB_TOWER};
    }
}
