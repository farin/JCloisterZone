package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.Set;

public class FairyNextToAction extends SelectFollowerAction {

    private final String figureId;

    public FairyNextToAction(String figureId, Set<MeeplePointer> options) {
        super(options);
        this.figureId = figureId;
    }

    @Override
     public WsInGameMessage select(MeeplePointer target) {
        return new MoveNeutralFigureMessage(figureId, target);
    }

    @Override
    public String toString() {
        return "move fairy";
    }

}
