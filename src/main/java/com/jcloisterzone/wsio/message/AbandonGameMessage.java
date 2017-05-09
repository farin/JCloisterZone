package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("ABANDON_GAME")
public class AbandonGameMessage implements WsInGameMessage {
    private String gameId;

    public AbandonGameMessage(String gameId) {
        super();
        this.gameId = gameId;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
