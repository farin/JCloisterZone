package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.collection.Set;

public class EscapeAction extends ReturnMeepleAction {

    public EscapeAction(Set<MeeplePointer> options) {
        super(options, ReturnMeepleSource.SIEGE_ESCAPE);
    }

    @Override
    public String toString() {
        return "escape via a neighboring cloister";
    }

}
