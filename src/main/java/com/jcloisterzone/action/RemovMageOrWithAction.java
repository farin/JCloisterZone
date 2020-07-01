package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.Set;

public class RemovMageOrWithAction extends AbstractPlayerAction<NeutralFigure<FeaturePointer>> {

    public RemovMageOrWithAction(Set<NeutralFigure<FeaturePointer>> options) {
        super(options);
    }

    @Override
    public WsInGameMessage select(NeutralFigure<FeaturePointer> option) {
        return new MoveNeutralFigureMessage(option.getId(), null);
    }

}
