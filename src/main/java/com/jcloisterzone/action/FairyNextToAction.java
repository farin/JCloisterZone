package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction
@LinkedImage("actions/fairy")
public class FairyNextToAction extends SelectFollowerAction {

    private final String figureId;

    public FairyNextToAction(String figureId, Set<MeeplePointer> options) {
        super(options);
        this.figureId = figureId;
    }

    @Override
    public void perform(GameController gc, MeeplePointer target) {
        gc.getConnection().send(
            new MoveNeutralFigureMessage(gc.getGameId(), figureId, target));
    }

    @Override
    public String toString() {
        return "move fairy";
    }

}
