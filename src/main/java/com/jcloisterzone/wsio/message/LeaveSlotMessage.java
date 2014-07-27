package com.jcloisterzone.wsio.message;

public class LeaveSlotMessage {

    private String gameId;
    private int number;

    public LeaveSlotMessage(String gameId, int number) {
        super();
        this.gameId = gameId;
        this.number = number;
    }

    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
}
