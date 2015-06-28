package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.RmiProxy;

public class TakePrisonerAction extends SelectFollowerAction {

    public TakePrisonerAction() {
        super("takeprisoner");
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer mp) {
        server.takePrisoner(mp);
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
