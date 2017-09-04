package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

//TODO generic NeutralMeepleAction ?
public class MageAndWitchAction extends SelectFeatureAction {

    private final NeutralFigure<FeaturePointer> figure;

    public MageAndWitchAction(NeutralFigure<FeaturePointer> figure, Set<FeaturePointer> options) {
        super(options);
        this.figure = figure;
    }

    @Override
    public WsInGameMessage select(FeaturePointer target) {
        return new MoveNeutralFigureMessage(figure.getId(), target);
    }

    public NeutralFigure<FeaturePointer> getFigure() {
        return figure;
    }

    @Override
    public String toString() {
        return "move " + figure;
    }
}
