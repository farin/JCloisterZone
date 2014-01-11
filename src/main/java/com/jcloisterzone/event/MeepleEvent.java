package com.jcloisterzone.event;

import com.jcloisterzone.figure.Meeple;

public class MeepleEvent extends PlayEvent {

    public static final int DEPLOY = 1;
    public static final int UNDEPLOY = 2;
    public static final int PRISON = 3;
    public static final int RELEASE = 4;

    private final Meeple meeple;

    public MeepleEvent(int type, Meeple meeple) {
        super(type, meeple.getPlayer(), meeple.getPosition(), meeple.getLocation());
        this.meeple = meeple;
    }

    public Meeple getMeeple() {
        return meeple;
    }
}
