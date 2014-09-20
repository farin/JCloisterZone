package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DRAW")
public class DrawMessage implements WsMessage {

    private String gameId;
    private int packSize;
    private int[] values;

    public DrawMessage(String gameId, int packSize, int[] values) {
        super();
        this.gameId = gameId;
        this.packSize = packSize;
        this.values = values;
    }

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
}
