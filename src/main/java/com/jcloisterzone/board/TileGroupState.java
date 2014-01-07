package com.jcloisterzone.board;

public enum TileGroupState {
    ACTIVE,  //active tiles which can be drawn
    WAITING, //tiles waiting for activation
    RETIRED  //inactive and doesn't count to total size
}