package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CLOCK")
public class ClockMessage implements WsInGameMessage {

    private String gameId;
    private Integer running;
    private int[] clocks;
    private long currentTime;


    public ClockMessage(String gameId, Integer running, int[] clocks, long currentTime) {
        super();
        this.gameId = gameId;
        this.running = running;
        this.clocks = clocks;
        this.currentTime = currentTime;
    }

    @Override
    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public int[] getClocks() {
        return clocks;
    }

    public void setClocks(int[] clocks) {
        this.clocks = clocks;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

}
