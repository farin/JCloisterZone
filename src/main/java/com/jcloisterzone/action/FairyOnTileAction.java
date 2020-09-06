package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import io.vavr.collection.Set;

public class FairyOnTileAction extends SelectTileAction {

    private final String figureId;

    public FairyOnTileAction(String figureId, Set<Position> options) {
        super(options);
        this.figureId = figureId;
    }

    public String getFigureId() {
        return figureId;
    }
}
