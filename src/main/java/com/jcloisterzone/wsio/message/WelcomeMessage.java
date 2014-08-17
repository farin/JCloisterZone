package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("WELCOME")
public class WelcomeMessage implements WsMessage {
    private String clientId;
    private String sessionKey;
    private String nickname;

    public WelcomeMessage(String clientId, String sessionKey, String nickname) {
        this.clientId = clientId;
        this.sessionKey = sessionKey;
        this.nickname = nickname;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
