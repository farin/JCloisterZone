package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("MAKE_DRAW")
public class MakeDrawMessage implements WsMessage {

    private String gameId;
    private int packSize, k;

    public MakeDrawMessage(String gameId, int packSize, int k) {
        this.gameId = gameId;
        this.packSize = packSize;
        this.k = k;
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

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
