package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MageWitchSelectRemoval;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.MageAndWitchCapability;

public class MageAndWitchPhase extends Phase {

    private final MageAndWitchCapability mwCap;

    public MageAndWitchPhase(Game game) {
        super(game);
        mwCap = game.getCapability(MageAndWitchCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(MageAndWitchCapability.class);
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

    @Override
    public void moveNeutralFigure(BoardPointer ptr, Class<? extends NeutralFigure> figureType) {
        FeaturePointer fp = (FeaturePointer) ptr;
        if (Mage.class.equals(figureType)) {
            mwCap.getMage().deploy(fp);
            next();
        } else if (Witch.class.equals(figureType)) {
            mwCap.getWitch().deploy(fp);
            next();
        } else {
            super.moveNeutralFigure(fp, figureType);
        }
    }
}
