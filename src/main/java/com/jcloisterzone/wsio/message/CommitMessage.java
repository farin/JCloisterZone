package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("COMMIT")
public class CommitMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage, WsSaltMessage {

    private String gameId;
    private long clock;
    private String parentId;
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
    public long getClock() {
        return clock;
    }

    @Override
    public void setClock(long clock) {
        this.clock = clock;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
