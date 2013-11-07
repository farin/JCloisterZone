package com.jcloisterzone.ai;

import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
    private final Operation operation;
    private final Phase phase;
    private final Object[] capabilitiesBackups;

    public SavePoint(Operation operation, Phase phase, Object[] capabilitiesBackups) {
        this.operation = operation;
        this.phase = phase;
        this.capabilitiesBackups = capabilitiesBackups;
    }

    public Operation getOperation() {
        return operation;
    }

    public Phase getPhase() {
        return phase;
    }

    public Object[] getCapabilitiesBackups() {
        return capabilitiesBackups;
    }
}