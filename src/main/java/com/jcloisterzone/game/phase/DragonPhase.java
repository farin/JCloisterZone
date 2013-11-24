package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;

public class DragonPhase extends Phase {

    private final DragonCapability dragonCap;

    public DragonPhase(Game game) {
        super(game);
        dragonCap = game.getCapability(DragonCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(DragonCapability.class);
    }

    @Override
    public void enter() {
        if (getTile().hasTrigger(TileTrigger.DRAGON)) {
            if (dragonCap.getDragonPosition() != null) {
                dragonCap.triggerDragonMove();
                next(DragonMovePhase.class);
                return;
            }
        }
        next();
    }






}
