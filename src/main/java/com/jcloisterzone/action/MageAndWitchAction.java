package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.wsio.RmiProxy;

//todo generic NeutralMeepleAction
public class MageAndWitchAction extends SelectFeatureAction {

    private final boolean mage;

    public MageAndWitchAction(boolean mage) {
        super(mage ? "mage": "witch");
        this.mage = mage;
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer target) {
        if (mage) {
            server.moveMage(target);
        } else {
            server.moveWitch(target);
        }
    }
}
