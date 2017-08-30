package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("MOVE_NEUTRAL_FIGURE")
public class MoveNeutralFigureMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String figureId;
    private BoardPointer to;

    public MoveNeutralFigureMessage() {
    }

    public MoveNeutralFigureMessage(String figureId, BoardPointer to) {
        this.figureId = figureId;
        this.to = to;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
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