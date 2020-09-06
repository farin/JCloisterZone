package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.Set;

public class FairyOnTileAction extends SelectTileAction {

    private final String figureId;

    public FairyOnTileAction(String figureId, Set<Position> options) {
        super(options);
        this.figureId = figureId;
    }

    @Override
     public Message select(Position target) {
        return new MoveNeutralFigureMessage(figureId, target);
    }

    @Override
    public String toString() {
        return "move fairy";
    }

    public String getFigureId() {
        return figureId;
    }
}
