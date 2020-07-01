package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.collection.Set;

public class PrincessAction extends ReturnMeepleAction {

    public PrincessAction(Set<MeeplePointer> options) {
        super(options, ReturnMeepleSource.PRINCESS);
    }

    @Override
    public String toString() {
        return "return meeple by princesss";
    }
}
