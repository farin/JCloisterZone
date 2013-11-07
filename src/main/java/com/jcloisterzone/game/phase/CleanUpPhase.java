package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BuilderCapability;

public class CleanUpPhase extends Phase {

    private final BuilderCapability builderCap;

    public CleanUpPhase(Game game) {
        super(game);
        builderCap = game.getCapability(BuilderCapability.class);
    }

    @Override
    public void enter() {
        boolean builderTakeAnotherTurn = builderCap != null && builderCap.hasPlayerAnotherTurn();
        game.turnCleanUp();
        game.setCurrentTile(null);
        if (builderTakeAnotherTurn) {
            next(game.hasCapability(AbbeyCapability.class) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            game.setTurnPlayer(game.getNextPlayer());
            next();
        }
    }

}
