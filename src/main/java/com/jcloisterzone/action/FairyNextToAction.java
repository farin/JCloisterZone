package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.wsio.RmiProxy;

public class FairyNextToAction extends SelectFollowerAction {

    public FairyNextToAction() {
        super("fairy");
    }

    @Override
    protected int getSortOrder() {
        return 30;
    }

    @Override
    public void perform(RmiProxy server, MeeplePointer target) {
        server.moveNeutralFigure(target, Fairy.class);
    }

    @Override
    public String toString() {
        return "move fairy";
    }

}
