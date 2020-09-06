package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.io.message.Message;
import com.jcloisterzone.io.message.MoveNeutralFigureMessage;
import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction ?
public class MoveDragonAction extends SelectTileAction {

    private final String figureId;

    public MoveDragonAction(String figureId, Set<Position> options) {
        super(options);
        this.figureId = figureId;
    }

    @Override
    public Message select(Position target) {
        return new MoveNeutralFigureMessage(figureId, target);
    }

    @Override
    public String toString() {
        return "move " + figureId;
    }

    public String getFigureId() {
        return figureId;
    }
}