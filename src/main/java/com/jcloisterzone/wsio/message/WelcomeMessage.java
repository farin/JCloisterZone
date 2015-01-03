package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("WELCOME")
public class WelcomeMessage implements WsMessage {
    private String sessionId;
    private String nickname;

    public WelcomeMessage(String sessionId, String nickname) {
        this.sessionId = sessionId;
        this.nickname = nickname;
    }

    public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
