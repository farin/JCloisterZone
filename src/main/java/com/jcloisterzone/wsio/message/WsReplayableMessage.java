package com.jcloisterzone.wsio.message;

public interface WsReplayableMessage extends WsChainedMessage, WsInGameMessage {

    long getClock();
    void setClock(long currentTime);

}
