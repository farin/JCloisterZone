package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("JOIN_GAME")
public class JoinGameMessage implements WsInGameMessage {
    private String gameId;
    private String password;

    public JoinGameMessage(String gameId) {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
