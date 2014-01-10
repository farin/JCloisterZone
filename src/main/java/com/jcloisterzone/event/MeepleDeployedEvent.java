package com.jcloisterzone.event;

import com.jcloisterzone.figure.Meeple;

public class MeepleDeployedEvent extends Event {

    private final Meeple meeple;

    public MeepleDeployedEvent(Meeple meeple) {
        super(meeple.getPlayer(), meeple.getPosition(), meeple.getLocation());
        this.meeple = meeple;
    }

    public Meeple getMeeple() {
        return meeple;
    }
}
