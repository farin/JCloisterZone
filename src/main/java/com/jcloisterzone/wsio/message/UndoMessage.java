package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("UNDO")
public class UndoMessage implements WsInGameMessage {

    private String gameId;
    /** notifies server hot to cut replay history,
     *  because server doesn't understand game rules */
    private int replaySize;

    public UndoMessage(String gameId, int replaySize) {
        super();
        this.gameId = gameId;
        this.replaySize = replaySize;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getReplaySize() {
        return replaySize;
    }

    public void setReplaySize(int replaySize) {
        this.replaySize = replaySize;
    }
}
