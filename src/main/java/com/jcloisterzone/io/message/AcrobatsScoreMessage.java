package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("ACROBATS_SCORE")
public class AcrobatsScoreMessage extends AbstractMessage implements ReplayableMessage {

    private FeaturePointer pointer;

    public AcrobatsScoreMessage() {
    }

    public AcrobatsScoreMessage(FeaturePointer pointer) {
        this.pointer = pointer;
    }

    public FeaturePointer getPointer() {
        return pointer;
    }

    public void setPointer(FeaturePointer pointer) {
        this.pointer = pointer;
    }

}