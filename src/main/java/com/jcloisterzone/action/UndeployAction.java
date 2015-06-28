package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.RmiProxy;

public class UndeployAction extends SelectFollowerAction {

    public UndeployAction(String name) {
        super(name);
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer mp) {
        server.undeployMeeple(mp);
    }

    @Override
    public String toString() {
        return "undeploy";
    }

}
