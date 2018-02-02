package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("POST_CHAT")
public class PostChatMessage extends AbstractWsMessage implements WsInGameMessage, WsInChannelMessage {

    private String gameId;
    private String channel;
    private String text;

    public PostChatMessage() {
    }

    public PostChatMessage(String text) {
        super();
        this.text = text;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }
}
