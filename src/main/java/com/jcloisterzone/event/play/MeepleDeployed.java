package com.jcloisterzone.event.play;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.Meeple;

public class MeepleDeployed extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private Meeple meeple;
    private BoardPointer ptr;

    public MeepleDeployed(PlayEventMeta metadata, Meeple meeple, BoardPointer ptr) {
        super(metadata);
        this.meeple = meeple;
        this.ptr = ptr;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public BoardPointer getPointer() {
        return ptr;
    }

    public Location getLocation() {
        return ptr.asFeaturePointer().getLocation();
    }


}
