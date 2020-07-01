package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.BridgeCapability.BrigeToken;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.Set;

//TODO generic token action ?

public class BridgeAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public BridgeAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(FeaturePointer ptr) {
        return new PlaceTokenMessage(BrigeToken.BRIDGE, ptr);
    }

    @Override
    public String toString() {
        return "place bridge";
    }



}
