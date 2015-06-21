package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MageWitchSelectRemoval;
import com.jcloisterzone.event.SelectActionEvent;
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
                        moveMage(null); //calls next()
                        return;
                    }
                    if (mwCap.getWitch().isDeployed()) {
                        moveWitch(null); //calls next()
                        return;
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
    public void moveMage(FeaturePointer fp) {
        mwCap.getMage().deploy(fp);
        next();
    }

    @Override
    public void moveWitch(FeaturePointer fp) {
        mwCap.getWitch().deploy(fp);
        next();
    }

}
