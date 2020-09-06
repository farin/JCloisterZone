package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import io.vavr.collection.Set;

public class TowerPieceAction extends SelectTileAction {

    public TowerPieceAction(Set<Position> options) {
        super(options);
    }

}
