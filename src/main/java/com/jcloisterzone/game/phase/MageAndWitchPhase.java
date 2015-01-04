package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.action.MageAndWitchAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.Wagon;
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
            MageAndWitchAction mageAction = mwCap.prepareMageAction();
            MageAndWitchAction witchAction = mwCap.prepareWitchAction();
            if (!mageAction.isEmpty() || !witchAction.isEmpty()) {
                List<PlayerAction<?>> actions = new ArrayList<>(2);
                if (!mageAction.isEmpty()) {
                    actions.add(mageAction);
                }
                if (!witchAction.isEmpty()) {
                    actions.add(witchAction);
                }
                game.post(new SelectActionEvent(getActivePlayer(), actions, false));
                return;
            } else {
                //TODO removee
            }
        }
        next();
    }

    @Override
    public void moveMage(FeaturePointer fp) {
        FeaturePointer oldPlacement = mwCap.getMagePlacement();
        mwCap.setMagePlacement(fp);
        game.post(new NeutralFigureMoveEvent(NeutralFigureMoveEvent.MAGE, getActivePlayer(), oldPlacement, fp));
        next();
    }

    @Override
    public void moveWitch(FeaturePointer fp) {
        FeaturePointer oldPlacement = mwCap.getWitchPlacement();
        mwCap.setWitchPlacement(fp);
        game.post(new NeutralFigureMoveEvent(NeutralFigureMoveEvent.WITCH, getActivePlayer(), oldPlacement, fp));
        next();
    }

}
