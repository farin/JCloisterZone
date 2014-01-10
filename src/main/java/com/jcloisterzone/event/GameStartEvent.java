package com.jcloisterzone.event;

import com.jcloisterzone.game.Snapshot;

public class GameStartEvent extends Event {

    private final Snapshot snapshot;

    public GameStartEvent(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

}
