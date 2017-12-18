package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("RETURN_MEEPLE")
public class ReturnMeepleMessage implements WsInGameMessage, WsReplayableMessage {

    public enum ReturnMeepleSource {
        PRINCESS, SIEGE_ESCAPE, FESTIVAL, CORN_CIRCLE;
    }

    private String gameId;
    private String messageId;
    private MeeplePointer pointer;
    private ReturnMeepleSource source;

    public ReturnMeepleMessage() {
    }

    public ReturnMeepleMessage(MeeplePointer pointer, ReturnMeepleSource source) {
        this.pointer = pointer;
        this.source = source;
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

    public ReturnMeepleSource getSource() {
        return source;
    }

    public void setSource(ReturnMeepleSource source) {
        this.source = source;
    }
}