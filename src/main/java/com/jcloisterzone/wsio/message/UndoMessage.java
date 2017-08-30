package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("UNDO")
public class UndoMessage implements WsInGameMessage {

    private String gameId;
    /** notifies server hot to cut replay history,
     *  because server doesn't understand game rules */
    private int replaySize;

    public UndoMessage() {
    }

    public UndoMessage(int replaySize) {
        this.replaySize = replaySize;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
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
