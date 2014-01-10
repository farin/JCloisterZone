package com.jcloisterzone.event;

import com.jcloisterzone.figure.Meeple;

//TODO freeze args here
public class MeepleUndeployedEvent extends Event {

    private final Meeple meeple;

    public MeepleUndeployedEvent(Meeple meeple) {
        super(meeple.getPlayer(), meeple.getPosition(), meeple.getLocation());
        this.meeple = meeple;
    }

    public Meeple getMeeple() {
        return meeple;
    }

}
