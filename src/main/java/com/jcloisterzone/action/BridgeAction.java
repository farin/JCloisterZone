package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

//TODO generic token action ?

@LinkedImage("actions/bridge")
@LinkedGridLayer(FeatureAreaLayer.class)
public class BridgeAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

    public BridgeAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(FeaturePointer ptr) {
        return new PlaceTokenMessage(Token.BRIDGE, ptr);
    }

    @Override
    public String toString() {
        return "place bridge";
    }



}
