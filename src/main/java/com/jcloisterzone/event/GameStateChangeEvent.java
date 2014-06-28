package com.jcloisterzone.event;

import com.jcloisterzone.game.Snapshot;

public class GameStateChangeEvent extends Event {

    public static final int GAME_START = 1;
    public static final int GAME_OVER = 2;

    private final Snapshot snapshot;

    public GameStateChangeEvent(int type) {
        this(type, null);
    }

    public GameStateChangeEvent(int type, Snapshot snapshot) {
        super(type);
        this.snapshot = snapshot;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

}
