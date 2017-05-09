package com.jcloisterzone.event.play;

import com.jcloisterzone.feature.Castle;

public class CastleCreated extends PlayEvent {

    private final Castle castle;

    public CastleCreated(PlayEventMeta metadata, Castle castle) {
        super(metadata);
        this.castle = castle;
    }

    public Castle getCastle() {
        return castle;
    }

}
