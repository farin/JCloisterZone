package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
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
            server.moveNeutralFigure(target, Mage.class);
        } else {
            server.moveNeutralFigure(target, Witch.class);
        }
    }
}
