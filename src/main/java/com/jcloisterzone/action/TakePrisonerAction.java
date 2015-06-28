package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.RmiProxy;

public class TakePrisonerAction extends SelectFollowerAction {

    public TakePrisonerAction() {
        super("takeprisoner");
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer bp) {
        server.takePrisoner(new FeaturePointer(bp.getPosition(), bp.getLocation()), bp.getMeepleType(), bp.getMeepleOwner().getIndex());
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
