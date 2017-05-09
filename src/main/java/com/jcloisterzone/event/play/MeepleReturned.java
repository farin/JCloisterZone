package com.jcloisterzone.event.play;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

public class MeepleReturned extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private Meeple meeple;
    private FeaturePointer from;

    public MeepleReturned(PlayEventMeta metadata, Meeple meeple, FeaturePointer from) {
        super(metadata);
        this.meeple = meeple;
        this.from = from;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public FeaturePointer getFrom() {
        return from;
    }

}
