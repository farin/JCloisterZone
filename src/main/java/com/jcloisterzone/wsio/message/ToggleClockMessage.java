package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("TOGGLE_CLOCK")
public class ToggleClockMessage implements WsInGameMessage {

    private String gameId;
    private Integer run;

    public ToggleClockMessage(String gameId, Integer run) {
        this.gameId = gameId;
        this.run = run;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Integer getRun() {
        return run;
    }

    public void setRun(Integer run) {
        this.run = run;
    }
}
