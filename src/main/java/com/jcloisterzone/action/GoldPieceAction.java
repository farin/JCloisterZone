package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import io.vavr.collection.Set;

public class GoldPieceAction extends SelectTileAction {

    public GoldPieceAction(Set<Position> options) {
        super(options);
    }
}

