package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.FerriesCapability.FerryToken;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.Set;

public class FerriesAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public FerriesAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(FeaturePointer option) {
        return new PlaceTokenMessage(FerryToken.FERRY, option);
    }
}
