package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("UNDO")
public class UndoMessage implements WsInGameMessage {

    private String gameId;
    /** notifies server hot to cut replay history,
     *  because server doesn't understand game rules */
    private String lastMessageId;

    public UndoMessage() {
    }

    public UndoMessage(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}
