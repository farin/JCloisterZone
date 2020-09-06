package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction ?
public class MoveDragonAction extends SelectTileAction {

    private final String figureId;

    public MoveDragonAction(String figureId, Set<Position> options) {
        super(options);
        this.figureId = figureId;
    }

    public String getFigureId() {
        return figureId;
    }
}