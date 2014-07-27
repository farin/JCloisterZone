package com.jcloisterzone.wsio.message;

public class WelcomeMessage {
    String clientId;
    String sessionKey;

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
