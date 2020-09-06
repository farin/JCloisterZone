package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("CIRCLE_REMOVE_OR_DEPLOY")
public class CornCircleRemoveOrDeployMessage extends AbstractMessage implements ReplayableMessage {

    public enum CornCircleOption { DEPLOY, REMOVE }

    private CornCircleOption value;

    public CornCircleRemoveOrDeployMessage() {
    }

    public CornCircleRemoveOrDeployMessage(CornCircleOption value) {
        this.value = value;
    }

    public CornCircleOption getValue() {
        return value;
    }

    public void setValue(CornCircleOption value) {
        this.value = value;
    }
}
