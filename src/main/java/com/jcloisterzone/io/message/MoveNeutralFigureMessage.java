package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("MOVE_NEUTRAL_FIGURE")
public class MoveNeutralFigureMessage extends AbstractMessage implements ReplayableMessage {

    private String figureId;
    private BoardPointer to;

    public MoveNeutralFigureMessage() {
    }

    public MoveNeutralFigureMessage(String figureId, BoardPointer to) {
        this.figureId = figureId;
        this.to = to;
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