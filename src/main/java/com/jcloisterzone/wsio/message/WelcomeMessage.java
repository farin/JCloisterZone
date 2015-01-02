package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("WELCOME")
public class WelcomeMessage implements WsMessage {
    private String clientId;
    private String nickname;

    public WelcomeMessage(String clientId, String nickname) {
        this.clientId = clientId;
        this.nickname = nickname;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
