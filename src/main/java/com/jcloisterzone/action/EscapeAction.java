package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;

import io.vavr.collection.Set;

@LinkedImage("actions/escape")
public class EscapeAction extends SelectFollowerAction {

    public EscapeAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, MeeplePointer ptr) {
        gc.getConnection().send(new ReturnMeepleMessage(
            gc.getGame().getGameId(), ptr, ReturnMeepleSource.SIEGE_ESCAPE));
    }

    @Override
    public String toString() {
        return "escape via a neighboring cloister";
    }

}
