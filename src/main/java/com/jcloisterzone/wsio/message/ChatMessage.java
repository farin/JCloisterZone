package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CHAT")
public class ChatMessage implements WsInGameMessage, WsInChannelMessage {

    private String gameId;
    private String channel;
    private String sessionId;
    private String text;

    public ChatMessage(String sessionId, String text) {
        this.sessionId = sessionId;
        this.text = text;
    }

    @Override
	public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
