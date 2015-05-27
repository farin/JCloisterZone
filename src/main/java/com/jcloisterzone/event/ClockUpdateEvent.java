package com.jcloisterzone.event;

import com.jcloisterzone.Player;


public class ClockUpdateEvent extends Event {

    final Player runningClockPlayer;


    public ClockUpdateEvent(Player runningClockPlayer) {
        this.runningClockPlayer = runningClockPlayer;
    }

    public boolean isClockRunning() {
        return runningClockPlayer != null;
    }

    public Player getRunningClockPlayer() {
        return runningClockPlayer;
    }
}
