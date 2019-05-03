package com.jcloisterzone.wsio.message;

public interface WsSaltMessage extends WsInGameMessage {
    long getSalt();
    void setSalt(long seed);
}
