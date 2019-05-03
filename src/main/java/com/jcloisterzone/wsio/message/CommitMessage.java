package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("COMMIT")
public class CommitMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage, WsSaltMessage {

    private String gameId;
    private String messageId;
    private long salt;


    public CommitMessage() {
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public long getSalt() {
        return salt;
    }

    @Override
    public void setSalt(long salt) {
        this.salt = salt;
    }
}
