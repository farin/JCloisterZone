package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("MOVE_NEUTRAL_FIGURE")
public class MoveNeutralFigureMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private long clock;
    private String parentId;
    private String figureId;
    private BoardPointer to;

    public MoveNeutralFigureMessage() {
    }

    public MoveNeutralFigureMessage(String figureId, BoardPointer to) {
        this.figureId = figureId;
        this.to = to;
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

    public String getFigureId() {
        return figureId;
    }

    public void setFigureId(String figureId) {
        this.figureId = figureId;
    }

    public BoardPointer getTo() {
        return to;
    }

    public void setTo(BoardPointer to) {
        this.to = to;
    }
}