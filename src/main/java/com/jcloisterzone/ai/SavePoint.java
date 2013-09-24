package com.jcloisterzone.ai;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.CapabilityController;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
    private final Operation operation;
    private final Phase phase;
    private final Map<Capability, CapabilityController> frozenCapabilities = Maps.newHashMap();

    public SavePoint(Operation operation, Game game) {
        this.operation = operation;
        this.phase = game.getPhase();
        for (Entry<Capability, CapabilityController> entry : game.getCapabilityMap().entrySet()) {
            CapabilityController copy = entry.getValue().copy();
            if (copy != null) {
                frozenCapabilities.put(entry.getKey(), copy);
            }
        }
    }

    public Operation getOperation() {
        return operation;
    }

    public Phase getPhase() {
        return phase;
    }

    public Map<Capability, CapabilityController> getFrozenCapabilities() {
        return frozenCapabilities;
    }
}