package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class CleanUpPhase extends Phase {

    public CleanUpPhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        boolean builderTakeAnotherTurn = game.hasCapability(Capability.BUILDER) && game.getBuilderCapability().hasPlayerAnotherTurn();
        game.extensionsDelegate().turnCleanUp();
        game.setCurrentTile(null);
        if (builderTakeAnotherTurn) {
            next(game.hasCapability(Capability.ABBEY) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            game.setTurnPlayer(game.getNextPlayer());
            next();
        }
    }

}
