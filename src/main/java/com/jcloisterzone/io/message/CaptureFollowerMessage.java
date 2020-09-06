package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("CAPTURE_FOLLOWER")
public class CaptureFollowerMessage extends AbstractMessage implements ReplayableMessage {

    private MeeplePointer pointer;

    public CaptureFollowerMessage() {
    }

    public CaptureFollowerMessage(MeeplePointer pointer) {
        this.pointer = pointer;
    }

    public MeeplePointer getPointer() {
        return pointer;
    }

    public void setPointer(MeeplePointer pointer) {
        this.pointer = pointer;
    }
}