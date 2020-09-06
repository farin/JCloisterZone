package com.jcloisterzone.io.message;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("DEPLOY_MEEPLE")
public class DeployMeepleMessage extends AbstractMessage implements ReplayableMessage, SaltMessage {

    private FeaturePointer pointer;
    private String meepleId;
    private String salt; // salted only for FLYING_MACHINE

    public DeployMeepleMessage() {
    }

    public DeployMeepleMessage(FeaturePointer pointer, String meepleId, String salt) {
        this.pointer = pointer;
        this.meepleId = meepleId;
        this.salt = salt;
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
    public String getSalt() {
        return salt;
    }

    @Override
    public void setSalt(String salt) {
        this.salt = salt;
    }
}
