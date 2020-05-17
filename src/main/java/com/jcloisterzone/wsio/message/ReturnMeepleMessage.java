package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("RETURN_MEEPLE")
public class ReturnMeepleMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    public enum ReturnMeepleSource {
        PRINCESS, SIEGE_ESCAPE, FESTIVAL, CORN_CIRCLE;
    }

    private String gameId;
    private long clock;
    private String parentId;

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
    public long getClock() {
        return clock;
    }

    @Override
    public void setClock(long clock) {
        this.clock = clock;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
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