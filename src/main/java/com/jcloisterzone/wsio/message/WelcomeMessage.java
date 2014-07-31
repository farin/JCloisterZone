package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.Cmd;

@Cmd("WELCOME")
public class WelcomeMessage implements WsMessage {
    private String clientId;
    private String sessionKey;

    public WelcomeMessage(String clientId, String sessionKey) {
        this.clientId = clientId;
        this.sessionKey = sessionKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

}
