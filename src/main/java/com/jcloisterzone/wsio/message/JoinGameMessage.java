package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.Cmd;

@Cmd("JOIN_GAME")
public class JoinGameMessage implements WsMessage {
    private String gameId;

    public JoinGameMessage(String gameId) {
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
