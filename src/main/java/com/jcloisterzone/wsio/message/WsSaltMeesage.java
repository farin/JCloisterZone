package com.jcloisterzone.wsio.message;

public interface WsSaltMeesage extends WsInGameMessage {
    long getSalt();
    void setSalt(long seed);
}
