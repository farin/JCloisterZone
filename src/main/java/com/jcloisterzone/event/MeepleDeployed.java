package com.jcloisterzone.event;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.Meeple;

public class MeepleDeployed extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private Meeple meeple;
    private BoardPointer ptr;
    private BoardPointer movedFrom;

    public MeepleDeployed(PlayEventMeta metadata, Meeple meeple, BoardPointer ptr, BoardPointer movedFrom) {
        super(metadata);
        this.meeple = meeple;
        this.ptr = ptr;
        this.movedFrom = movedFrom;
    }

    public MeepleDeployed(PlayEventMeta metadata, Meeple meeple, BoardPointer ptr) {
        this(metadata, meeple, ptr, null);
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

    public BoardPointer getMovedFrom() {
        return movedFrom;
    }
}
