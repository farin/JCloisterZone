package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.Set;

public class TunnelAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    private final Tunnel token;

    public TunnelAction(Set<FeaturePointer> options, Tunnel token) {
        super(options);
        this.token = token;
    }

    public Tunnel getToken() {
        return token;
    }

    @Override
    public WsInGameMessage select(FeaturePointer ptr) {
        return new PlaceTokenMessage(token, ptr);
    }

    @Override
    public String toString() {
        return "place tunnel";
    }

}
