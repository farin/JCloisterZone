package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;

public class DragonPhase extends Phase {

    public DragonPhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(Capability.DRAGON);
    }

    @Override
    public void enter() {
        if (getTile().getTrigger() == TileTrigger.DRAGON) {
            DragonCapability dgCap = game.getDragonCapability();
            if (dgCap.getDragonPosition() != null) {
                dgCap.triggerDragonMove();
                next(DragonMovePhase.class);
                return;
            }
        }
        next();
    }






}
