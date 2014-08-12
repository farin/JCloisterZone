package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("POST_CHAT")
public class PostChatMessage implements WsMessage {

    private String gameId;
    private String text;

    public PostChatMessage(String gameId, String text) {
        super();
        this.gameId = gameId;
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
}
