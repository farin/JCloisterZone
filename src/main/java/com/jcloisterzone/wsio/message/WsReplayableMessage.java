package com.jcloisterzone.wsio.message;

public interface WsReplayableMessage extends WsInGameMessage {

    String getMessageId();
    void setMessageId(String messageId);

}
