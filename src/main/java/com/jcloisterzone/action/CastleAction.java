package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.wsio.RmiProxy;

public class CastleAction extends SelectFeatureAction {

    public CastleAction() {
        super("castle");
    }

    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployCastle(bp.getPosition(), bp.getLocation());
    }

    @Override
    public String toString() {
        return "place castle";
    }

}
