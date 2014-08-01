package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.rmi.RmiProxy;

public class UndeployAction extends SelectFollowerAction {

    public UndeployAction(String name) {
        super(name);
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer bp) {
        server.undeployMeeple(bp.getPosition(), bp.getLocation(), bp.getMeepleType(), bp.getMeepleOwner().getIndex());
    }

    @Override
    public String toString() {
        return "undeploy";
    }

}
