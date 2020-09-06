package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("RETURN_MEEPLE")
public class ReturnMeepleMessage extends AbstractMessage implements ReplayableMessage {

    public enum ReturnMeepleSource {
        PRINCESS, SIEGE_ESCAPE, FESTIVAL, CORN_CIRCLE, ABBOT_RETURN
    }

    private MeeplePointer pointer;
    private ReturnMeepleSource source;

    public ReturnMeepleMessage() {
    }

    public ReturnMeepleMessage(MeeplePointer pointer, ReturnMeepleSource source) {
        this.pointer = pointer;
        this.source = source;
    }

    public MeeplePointer getPointer() {
        return pointer;
    }

    public void setPointer(MeeplePointer pointer) {
        this.pointer = pointer;
    }

    public ReturnMeepleSource getSource() {
        return source;
    }

    public void setSource(ReturnMeepleSource source) {
        this.source = source;
    }
}