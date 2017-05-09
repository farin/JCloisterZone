package com.jcloisterzone.event;

import com.jcloisterzone.PlayerClock;

import io.vavr.collection.Array;

public class ClockUpdateEvent extends Event {

    final Array<PlayerClock> clocks;
    final Integer running;

    public ClockUpdateEvent(Array<PlayerClock> clocks, Integer running) {
        this.clocks = clocks;
        this.running = running;
    }

    public boolean isClockRunning() {
        return running != null;
    }

    public Array<PlayerClock> getClocks() {
        return clocks;
    }

    public Integer getRunning() {
        return running;
    }
}
