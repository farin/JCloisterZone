package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Game;

/**
 * real end of turn and switch to next player
 */
public class CleanUpTurnPhase extends Phase {

    public CleanUpTurnPhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        game.turnCleanUp();
        game.setTurnPlayer(game.getNextPlayer());
        next();
    }
}
