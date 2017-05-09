package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MageWitchSelectRemoval;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;

@RequiredCapability(MageAndWitchCapability.class)
public class MageAndWitchPhase extends Phase {

    public MageAndWitchPhase(GameController gc) {
        super(gc);
    }

    @Override
    public void enter() {
        if (getTile().hasTrigger(TileTrigger.MAGE) || mwCap.isMageAndWitchPlacedOnSameFeature()) {
            List<PlayerAction<?>> actions = mwCap.prepareMageWitchActions();
            if (actions == null) { //force removal
                if (mwCap.getMage().isDeployed() && mwCap.getWitch().isDeployed()) {
                    game.post(new MageWitchSelectRemoval(getActivePlayer(), getActivePlayer()));
                    return;
                } else {
                    if (mwCap.getMage().isDeployed()) {
                        mwCap.getMage().deploy(null);
                    }
                    if (mwCap.getWitch().isDeployed()) {
                        mwCap.getWitch().deploy(null);
                    }
                }
            } else {
                game.post(new SelectActionEvent(getActivePlayer(), actions, false));
                return;
            }
        }
        next();
    }

    @WsSubscribe
    public void handleMoveNeutralFigure(MoveNeutralFigureMessage msg) {
        GameState state = game.getState();
        FeaturePointer ptr = (FeaturePointer) msg.getTo();
        @SuppressWarnings("unchecked")
        NeutralFigure<FeaturePointer> fig = (NeutralFigure<FeaturePointer>) state.getNeutralFigures().getById(msg.getFigureId());

        if (fig instanceof Mage || fig instanceof Witch) {
            state = (new MoveNeutralFigure<FeaturePointer>(fig, ptr, state.getActivePlayer())).apply(state);
            state = clearActions(state);
            next(state);
        } else {
            throw new IllegalArgumentException("Illegal neutral figure move");
        }
    }
}
