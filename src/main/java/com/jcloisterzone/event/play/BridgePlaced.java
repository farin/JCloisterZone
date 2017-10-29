package com.jcloisterzone.event.play;

import com.jcloisterzone.board.pointer.FeaturePointer;

//TODO merge with TokenPlacedEvent?
public class BridgePlaced extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final FeaturePointer pointer;

    public BridgePlaced(PlayEventMeta metadata, FeaturePointer pointer) {
        super(metadata);
        this.pointer = pointer;
    }

    public FeaturePointer getPointer() {
        return pointer;
    }
}
