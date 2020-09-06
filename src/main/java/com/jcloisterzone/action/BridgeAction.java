package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.BridgeCapability.BrigeToken;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.Set;

//TODO generic token action ?

public class BridgeAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public BridgeAction(Set<FeaturePointer> options) {
        super(options);
    }

}
