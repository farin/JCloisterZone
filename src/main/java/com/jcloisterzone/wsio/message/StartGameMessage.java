package com.jcloisterzone.wsio.message;

public class StartGameMessage {
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
