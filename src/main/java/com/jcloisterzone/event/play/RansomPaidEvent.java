package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;

public class RansomPaidEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private Follower follower;
    private Player jailer;

    public RansomPaidEvent(PlayEventMeta metadata, Follower follower, Player jailer) {
        super(metadata);
        this.follower = follower;
        this.jailer = jailer;
    }

    public Meeple getMeeple() {
        return follower;
    }

    public Player getJailer() {
        return jailer;
    }
}
