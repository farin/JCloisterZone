package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.capability.CastleCapability.CastleToken;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import io.vavr.collection.Set;

public class CastleAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public CastleAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public Message select(FeaturePointer ptr) {
        return new PlaceTokenMessage(CastleToken.CASTLE, ptr);
    }

    @Override
    public String toString() {
        return "place castle";
    }

}
