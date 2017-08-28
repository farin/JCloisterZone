package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;

import io.vavr.collection.Set;

@LinkedImage("actions/castle")
public class CastleAction extends SelectFeatureAction {

    public CastleAction(Set<FeaturePointer> options) {
        super(options);
    }

    public void perform(GameController gc, FeaturePointer ptr) {
        gc.getConnection().send(
            new PlaceTokenMessage(
                gc.getGameId(), Token.CASTLE, ptr
            )
        );
    }

    @Override
    public String toString() {
        return "place castle";
    }

}
