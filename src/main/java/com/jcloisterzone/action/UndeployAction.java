package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.rmi.Client2ClientIF;

public class UndeployAction extends SelectFollowerAction {

    public UndeployAction(String name) {
        super(name);
    }

    @Override
    public void perform(Client2ClientIF server, MeeplePointer bp) {
        server.undeployMeeple(bp.getPosition(), bp.getLocation(), bp.getMeepleType(), bp.getMeepleOwner().getIndex());
    }

    @Override
    public String toString() {
        return "undeploy";
    }

}
