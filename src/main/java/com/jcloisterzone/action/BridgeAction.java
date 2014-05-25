package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.rmi.Client2ClientIF;

public class BridgeAction extends SelectFeatureAction {

    public BridgeAction() {
        super("bridge");
    }

    @Override
    public void perform(Client2ClientIF server, FeaturePointer bp) {
        server.deployBridge(bp.getPosition(), bp.getLocation());
    }

}
