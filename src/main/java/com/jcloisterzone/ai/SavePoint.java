package com.jcloisterzone.ai;

import com.jcloisterzone.event.Undoable;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
    private final Undoable operation;
    private final Phase phase;
    private final Object[] capabilitiesBackups;

    //testing purposes only
    private Snapshot snapshot;

    public SavePoint(Undoable operation, Phase phase, Object[] capabilitiesBackups) {
        this.operation = operation;
        this.phase = phase;
        this.capabilitiesBackups = capabilitiesBackups;
    }

    public Undoable getOperation() {
        return operation;
    }

    public Phase getPhase() {
        return phase;
    }

    public Object[] getCapabilitiesBackups() {
        return capabilitiesBackups;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }


}