package com.jcloisterzone.wsio.message;

public interface WsInChannelMessage extends WsMessage {

    String getChannel();
    void setChannel(String channel);
}
