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

    public ReturnMeepleSource getSource() {
        return source;
    }
}
