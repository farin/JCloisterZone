package com.jcloisterzone.ai;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.Phase;

public class SavePoint {
    private final Operation operation;
    private final Phase phase;
    private final List<Capability> savedCapabilities;

    public SavePoint(Operation operation, final Game game) {
        this.operation = operation;
        this.phase = game.getPhase();
        savedCapabilities = Lists.transform(game.getCapabilities(), new Function<Capability, Capability>() {
            @Override
            public Capability apply(Capability cap) {
                return cap.copy(game);
            }
        });
    }

    public Operation getOperation() {
        return operation;
    }

    public Phase getPhase() {
        return phase;
    }

    public List<Capability> getSavedCapabilities() {
        return savedCapabilities;
    }
}