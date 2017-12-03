package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("COMMIT")
public class CommitMessage implements WsInGameMessage, WsReplayableMessage, WsSaltMeesage {

    private String gameId;
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
    public long getSalt() {
        return salt;
    }

    @Override
    public void setSalt(long salt) {
        this.salt = salt;
    }
}
