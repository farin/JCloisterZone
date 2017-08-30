package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;

import io.vavr.collection.Set;

@LinkedImage("actions/undeploy")
public class ReturnMeepleAction extends SelectFollowerAction {

    private final ReturnMeepleSource source;

    public ReturnMeepleAction(Set<MeeplePointer> options, ReturnMeepleSource source) {
        super(options);
        this.source = source;
    }

    @Override
    public void perform(GameController gc, MeeplePointer ptr) {
        gc.getConnection().send(new ReturnMeepleMessage(
            gc.getGame().getGameId(), ptr, source));
    }

    @Override
    public String toString() {
        return "return meeple";
    }
}
