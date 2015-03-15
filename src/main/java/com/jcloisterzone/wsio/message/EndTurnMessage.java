package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("END_TURN")
public class EndTurnMessage implements WsInGameMessage {

    private String gameId;
    private long currentTime;


    public EndTurnMessage(String gameId) {
        this.gameId = gameId;
        this.currentTime = System.currentTimeMillis();
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
