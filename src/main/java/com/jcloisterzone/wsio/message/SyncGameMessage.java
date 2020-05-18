package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("SYNC_GAME")
public class SyncGameMessage extends AbstractWsMessage implements WsInGameMessage {

    private String gameId;

    public SyncGameMessage() {
    }

    public SyncGameMessage(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
