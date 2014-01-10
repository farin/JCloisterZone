package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Meeple;

public class RansomPaidEvent extends Event {

    private final Player jailer;
    private final Meeple meeple;

    public RansomPaidEvent(Player player, Player jailer, Meeple meeple) {
        super(player);
        this.jailer = jailer;
        this.meeple = meeple;
    }

    public Player getJailer() {
        return jailer;
    }

    public Meeple getMeeple() {
        return meeple;
    }



}
