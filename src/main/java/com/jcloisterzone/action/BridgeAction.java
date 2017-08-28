package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.RmiProxy;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;

import io.vavr.collection.Set;

//TODO generic token action ?

@LinkedImage("actions/bridge")
public class BridgeAction extends SelectFeatureAction {

    public BridgeAction(Set<FeaturePointer> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, FeaturePointer ptr) {
        gc.getConnection().send(
            new PlaceTokenMessage(gc.getGameId(), Token.BRIDGE, ptr)
        );
    }

    @Override
    public String toString() {
        return "place bridge";
    }



}
