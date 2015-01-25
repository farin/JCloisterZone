package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("LEAVE_GAME")
public class LeaveGameMessage implements WsInGameMessage {
    private String gameId;

    public LeaveGameMessage(String gameId) {
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
