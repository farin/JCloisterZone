package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.rmi.RmiProxy;

public class BridgeAction extends SelectFeatureAction {

    public BridgeAction() {
        super("bridge");
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployBridge(bp.getPosition(), bp.getLocation());
    }

    @Override
    public String toString() {
        return "place bridge";
    }

}
