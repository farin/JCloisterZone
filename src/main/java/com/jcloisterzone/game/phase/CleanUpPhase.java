package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Game;

public class CleanUpPhase extends Phase {

    public CleanUpPhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        boolean builderTakeAnotherTurn = game.hasExpansion(Expansion.TRADERS_AND_BUILDERS) && game.getTradersAndBuildersGame().hasPlayerAnotherTurn();
        game.expansionDelegate().turnCleanUp();
        game.getTilePack().cleanUpTurn();
        if (builderTakeAnotherTurn) {
            next(game.hasExpansion(Expansion.ABBEY_AND_MAYOR) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            game.setTurnPlayer(game.getNextPlayer());
            next();
        }

    }

}
