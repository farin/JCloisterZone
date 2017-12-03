package com.jcloisterzone.event.play;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;

public class NeutralFigureReturned extends PlayEvent {

    private final BoardPointer from;
    private final NeutralFigure<?> neutralFigure;

    public NeutralFigureReturned(PlayEventMeta metadata, NeutralFigure<?> neutralFigure, BoardPointer from) {
        super(metadata);
        this.neutralFigure = neutralFigure;
        this.from = from;
    }

    public BoardPointer getFrom() {
        return from;
    }

    public NeutralFigure<?> getNeutralFigure() {
        return neutralFigure;
    }
}
