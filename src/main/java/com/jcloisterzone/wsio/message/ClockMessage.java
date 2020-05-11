package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CLOCK")
public class ClockMessage extends AbstractWsMessage implements WsInGameMessage {

    private String gameId;
    private Integer running;
    private long[] clocks;
    private long currentTime;

    public ClockMessage(Integer running, long[] clocks, long currentTime) {
        this.running = running;
        this.clocks = clocks;
        this.currentTime = currentTime;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public long[] getClocks() {
        return clocks;
    }

}
