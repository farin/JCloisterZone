package com.jcloisterzone.event.play;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;

public class NeutralFigureMoved extends PlayEvent {

    private final BoardPointer from;
    private final BoardPointer to;
    private final NeutralFigure<?> neutralFigure;

    public NeutralFigureMoved(PlayEventMeta metadata, NeutralFigure<?> neutralFigure,
            BoardPointer from, BoardPointer to) {
        super(metadata);
        this.neutralFigure = neutralFigure;
        this.from = from;
        this.to = to;
    }

    public BoardPointer getFrom() {
        return from;
    }

    public BoardPointer getTo() {
        return to;
    }

    public NeutralFigure<?> getNeutralFigure() {
        return neutralFigure;
    }
}
