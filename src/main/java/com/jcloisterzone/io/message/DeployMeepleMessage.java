package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("DEPLOY_MEEPLE")
public class DeployMeepleMessage extends AbstractMessage implements ReplayableMessage, RandomChangingMessage {

    private FeaturePointer pointer;
    private String meepleId;
    private Double random; // set only for FLYING_MACHINE

    public DeployMeepleMessage() {
    }

    public DeployMeepleMessage(FeaturePointer pointer, String meepleId, Double random) {
        this.pointer = pointer;
        this.meepleId = meepleId;
        this.random = random;
    }

    public DeployMeepleMessage(FeaturePointer pointer, String meepleId) {
        this(pointer, meepleId, null);
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

    @Override
    public Double getRandom() {
        return random;
    }

    @Override
    public void setRandom(Double random) {
        this.random = random;
    }
}
