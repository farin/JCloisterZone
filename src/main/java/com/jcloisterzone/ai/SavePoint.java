package com.jcloisterzone.ai;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameExtension;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
    private final Operation operation;
    private final Phase phase;
    private final Map<Object, GameExtension> frozenExtensions = Maps.newHashMap();

    public SavePoint(Operation operation, Game game) {
        this.operation = operation;
        this.phase = game.getPhase();
        for(Entry<Object, GameExtension> entry : game.getExtensionMap().entrySet()) {
            GameExtension copy = entry.getValue().copy();
            if (copy != null) {
                frozenExtensions.put(entry.getKey(), copy);
            }
        }
    }

    public Operation getOperation() {
        return operation;
    }

    public Phase getPhase() {
        return phase;
    }

    public Map<Object, GameExtension> getFrozenExtensions() {
        return frozenExtensions;
    }
}