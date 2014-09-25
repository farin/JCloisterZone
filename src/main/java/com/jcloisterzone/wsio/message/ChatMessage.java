package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CHAT")
public class ChatMessage implements WsInGameMessage {

    private String gameId;
    private String clientId;
    private String text;

    public ChatMessage(String gameId, String clientId, String text) {
        this.gameId = gameId;
        this.clientId = clientId;
        this.text = text;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
