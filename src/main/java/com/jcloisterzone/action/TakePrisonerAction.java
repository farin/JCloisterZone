package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public class TakePrisonerAction extends SelectFollowerAction {

    public TakePrisonerAction() {
        super("takeprisoner");
    }

    @Override
    public void perform(Client2ClientIF server, Position pos, Location loc) {
        server.takePrisoner(pos, loc, getMeepleType(pos, loc));
    }

}
