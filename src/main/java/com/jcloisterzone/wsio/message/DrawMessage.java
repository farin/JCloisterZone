package com.jcloisterzone.wsio.message;

import java.util.Arrays;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DRAW")
public class DrawMessage implements WsInGameMessage {

    private String gameId;
    private int packSize;
    private int[] values;

    public DrawMessage(String gameId, int packSize, int[] values) {
        super();
        this.gameId = gameId;
        this.packSize = packSize;
        this.values = values;
    }

    @Override
	public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getPackSize() {
        return packSize;
    }

    public void setPackSize(int packSize) {
        this.packSize = packSize;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
        sb.append("DRAW {\"gameID\": \"").append(gameId).append("\", \"packSize\": \"")
        .append(packSize).append(", \"values\": ").append(Arrays.toString(values)).append("\"}");
        return sb.toString();
    }
}
