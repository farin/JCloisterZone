package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction
public class MageAndWitchAction extends SelectFeatureAction {

    private final String figureId;

    public MageAndWitchAction(String figureId, Set<FeaturePointer> options) {
        super(options);
        this.figureId = figureId;
    }

    @Override
    public void perform(GameController gc, FeaturePointer target) {
        gc.getConnection().send(new MoveNeutralFigureMessage(figureId, target));
    }

    public String getFigureId() {
        return figureId;
    }

    @Override
    public String toString() {
        return "move " + figureId;
    }
}
