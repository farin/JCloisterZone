package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.ReturnMeepleMessage;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.collection.Set;

public class ReturnMeepleAction extends AbstractPlayerAction<MeeplePointer> {

    private final ReturnMeepleSource source;

    public ReturnMeepleAction(Set<MeeplePointer> options, ReturnMeepleSource source) {
        super(options);
        this.source = source;
    }

    @Override
    public Message select(MeeplePointer ptr) {
        return new ReturnMeepleMessage(ptr, source);
    }

    @Override
    public String toString() {
        return "return meeple";
    }

    public ReturnMeepleSource getSource() {
        return source;
    }
}
