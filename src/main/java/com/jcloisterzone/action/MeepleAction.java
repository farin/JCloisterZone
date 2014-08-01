package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.rmi.RmiProxy;

public class MeepleAction extends SelectFeatureAction {

    private final Class<? extends Meeple> meepleType;

    public MeepleAction(Class<? extends Meeple> meepleType) {
        super(meepleType.getSimpleName().toLowerCase());
        this.meepleType = meepleType;
    }


    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer bp) {
        server.deployMeeple(bp.getPosition(), bp.getLocation(), meepleType);
    }

    @Override
    protected int getSortOrder() {
        if (meepleType.equals(SmallFollower.class)) return 10;
        if (meepleType.equals(BigFollower.class)) return 11;
        if (meepleType.equals(Wagon.class)) return 12;
        if (meepleType.equals(Mayor.class)) return 13;
        if (meepleType.equals(Builder.class)) return 14;
        if (meepleType.equals(Pig.class)) return 15;
        if (meepleType.equals(Phantom.class)) return 16;
        return 19;
    }

    @Override
    public String toString() {
        return "place " + meepleType.getSimpleName();
    }
}
