package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("UNDO")
public class UndoMessage extends AbstractMessage implements Message {

    public UndoMessage() {
    }
}
