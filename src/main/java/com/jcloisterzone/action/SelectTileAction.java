package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;

import io.vavr.collection.Set;

public abstract class SelectTileAction extends PlayerAction<Position> {

    public SelectTileAction(Set<Position> options) {
        super(options);
    }
}
