package com.jcloisterzone.wsio;

import com.jcloisterzone.wsio.message.WsMessage;

public interface Connection {

    public void send(WsMessage arg);
    public void close();
    public String getSessionId();
    public String getNickname();

}
