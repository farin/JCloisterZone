package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.Cmd;

@Cmd("START_GAME")
public class StartGameMessage implements WsMessage {
    private String gameId;

    public StartGameMessage(String gameId) {
        super();
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
