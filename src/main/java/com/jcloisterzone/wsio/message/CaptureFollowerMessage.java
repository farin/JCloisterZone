package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("CAPTURE_FOLLOWER")
public class CaptureFollowerMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String messageId;
    private MeeplePointer pointer;

    public CaptureFollowerMessage() {
    }

    public CaptureFollowerMessage(MeeplePointer pointer) {
        this.pointer = pointer;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MeeplePointer getPointer() {
        return pointer;
    }

    public void setPointer(MeeplePointer pointer) {
        this.pointer = pointer;
    }
}