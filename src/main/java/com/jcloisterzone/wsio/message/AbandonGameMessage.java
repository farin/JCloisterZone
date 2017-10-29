package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("ABANDON_GAME")
public class AbandonGameMessage implements WsMessage { // althoug contiains gameId, it is not WsInGameMessage message
    private String gameId;

    public AbandonGameMessage() {
    }

    public AbandonGameMessage(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
