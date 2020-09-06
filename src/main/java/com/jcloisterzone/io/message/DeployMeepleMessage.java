package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("DEPLOY_MEEPLE")
public class DeployMeepleMessage extends AbstractMessage implements ReplayableMessage {

    private FeaturePointer pointer;
    private String meepleId;

    public DeployMeepleMessage() {
    }

    public DeployMeepleMessage(FeaturePointer pointer, String meepleId) {
        this.pointer = pointer;
        this.meepleId = meepleId;
    }


    public FeaturePointer getPointer() {
        return pointer;
    }

    public void setPointer(FeaturePointer pointer) {
        this.pointer = pointer;
    }

    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }
}
