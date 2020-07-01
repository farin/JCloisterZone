package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.message.CaptureFollowerMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.Set;

public class CaptureFollowerAction extends SelectFollowerAction {

    public CaptureFollowerAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(MeeplePointer mp) {
        return new CaptureFollowerMessage(mp);
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
