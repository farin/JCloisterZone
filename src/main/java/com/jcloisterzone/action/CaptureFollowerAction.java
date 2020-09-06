package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.io.message.CaptureFollowerMessage;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.Set;

public class CaptureFollowerAction extends AbstractPlayerAction<MeeplePointer> {

    public CaptureFollowerAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public Message select(MeeplePointer mp) {
        return new CaptureFollowerMessage(mp);
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
