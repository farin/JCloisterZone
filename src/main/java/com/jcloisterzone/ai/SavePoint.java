package com.jcloisterzone.ai;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
    private final Operation operation;
    private final Phase phase;
    private final List<Capability> frozenCapabilities = new ArrayList<>();

    public SavePoint(Operation operation, Game game) {
        this.operation = operation;
        this.phase = game.getPhase();
        for (Capability cap : game.getCapabilities()) {
            Capability copy = cap.copy();
            //TODO !!!! change all capabilities to return its copy
            if (copy != null) {
                frozenCapabilities.add(copy);
            }
        }
    }

    public Operation getOperation() {
        return operation;
    }

    public Phase getPhase() {
        return phase;
    }

    public List<Capability> getFrozenCapabilities() {
        return frozenCapabilities;
    }

    public Capability getFrozenCapability(Class<? extends Capability> clazz) {
        for (Capability c : frozenCapabilities) {
            if (c.getClass().equals(clazz)) return c;
        }
        return null;
    }
}