package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.rmi.Client2ClientIF;

public class TakePrisonerAction extends SelectFollowerAction {

    public TakePrisonerAction() {
        super("takeprisoner");
    }

    @Override
    public void perform(Client2ClientIF server, MeeplePointer bp) {
    	server.takePrisoner(bp.getPosition(), bp.getLocation(), bp.getMeepleType(), bp.getMeepleOwner().getIndex());
    }


}
