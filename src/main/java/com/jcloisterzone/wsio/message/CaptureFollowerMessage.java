package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CAPTURE_FOLLOWER")
public class CaptureFollowerMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private MeeplePointer pointer;

    public CaptureFollowerMessage(String gameId, MeeplePointer pointer) {
        super();
        this.gameId = gameId;
        this.pointer = pointer;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public MeeplePointer getPointer() {
        return pointer;
    }

    public void setPointer(MeeplePointer pointer) {
        this.pointer = pointer;
    }
}