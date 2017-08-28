package com.jcloisterzone.wsio.message;

public interface WsSeedMeesage extends WsInGameMessage {
    long getSeed();
    void setSeed(long seed);
}
