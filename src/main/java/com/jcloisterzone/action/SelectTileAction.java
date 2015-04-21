package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;

public abstract class SelectTileAction extends PlayerAction<Position> {

    public SelectTileAction(String name) {
        super(name);
    }

}
