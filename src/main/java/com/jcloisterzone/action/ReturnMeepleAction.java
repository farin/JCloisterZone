package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage.ReturnMeepleSource;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

@LinkedImage("actions/undeploy")
public class ReturnMeepleAction extends SelectFollowerAction {

    private final ReturnMeepleSource source;

    public ReturnMeepleAction(Set<MeeplePointer> options, ReturnMeepleSource source) {
        super(options);
        this.source = source;
    }

    @Override
    public WsInGameMessage select(MeeplePointer ptr) {
        return new ReturnMeepleMessage(ptr, source);
    }

    @Override
    public String toString() {
        return "return meeple";
    }
}
