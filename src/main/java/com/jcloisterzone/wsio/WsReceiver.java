package com.jcloisterzone.wsio;

import com.jcloisterzone.wsio.message.WsMessage;

public interface WsReceiver {

    void onWebsocketClose(int code, String reason, boolean remote);
    void onWebsocketError(Exception ex);
    //call additionally to WsSubscribe
    void onWebsocketMessage(WsMessage msg);

}
