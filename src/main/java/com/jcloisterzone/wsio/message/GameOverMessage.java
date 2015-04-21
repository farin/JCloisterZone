package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME_OVER")
public class GameOverMessage implements WsInGameMessage {

	private String gameId;

    public GameOverMessage(String gameId) {
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
