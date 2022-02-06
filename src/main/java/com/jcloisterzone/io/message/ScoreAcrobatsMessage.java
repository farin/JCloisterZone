package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("SCORE_ACROBATS")
public class ScoreAcrobatsMessage extends AbstractMessage implements ReplayableMessage {

    private FeaturePointer pointer;

    public ScoreAcrobatsMessage() {
    }

    public ScoreAcrobatsMessage(FeaturePointer pointer) {
        this.pointer = pointer;
    }

    public FeaturePointer getPointer() {
        return pointer;
    }

    public void setPointer(FeaturePointer pointer) {
        this.pointer = pointer;
    }

}