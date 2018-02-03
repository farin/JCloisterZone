package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.SelectMageWitchRemovalPanel;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

@LinkedPanel(SelectMageWitchRemovalPanel.class)
public class RemovMageOrWithAction extends AbstractPlayerAction<NeutralFigure<FeaturePointer>> {

    public RemovMageOrWithAction(Set<NeutralFigure<FeaturePointer>> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(NeutralFigure<FeaturePointer> option) {
        return new MoveNeutralFigureMessage(option.getId(), null);
    }

}
