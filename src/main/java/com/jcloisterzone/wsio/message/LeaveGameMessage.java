package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("LEAVE_GAME")
public class LeaveGameMessage extends AbstractWsMessage implements WsInGameMessage {
    private String gameId;

    public LeaveGameMessage() {
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
