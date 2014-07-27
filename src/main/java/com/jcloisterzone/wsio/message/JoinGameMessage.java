package com.jcloisterzone.wsio.message;

public class JoinGameMessage {
    String gameId;

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
