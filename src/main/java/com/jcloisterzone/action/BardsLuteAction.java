package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.BardsLuteCapability;
import io.vavr.collection.Set;

public class BardsLuteAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    private final BardsLuteCapability.BardsLuteToken token;

    public BardsLuteAction(Set<FeaturePointer> options, BardsLuteCapability.BardsLuteToken token) {
        super(options);
        this.token = token;
    }

    public BardsLuteCapability.BardsLuteToken getToken() {
        return token;
    }
}
