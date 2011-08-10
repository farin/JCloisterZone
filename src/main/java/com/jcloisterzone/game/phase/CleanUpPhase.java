package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.Game;

public class CleanUpPhase extends Phase {

	public CleanUpPhase(Game game) {
		super(game);
	}

	@Override
	public void enter() {
		boolean builderTakeAnotherTurn = game.hasExpansion(Expansion.TRADERS_AND_BUILDERS) && game.getTradersAndBuildersGame().takeAnotherTurn();
		game.expansionDelegate().turnCleanUp();
		if (! builderTakeAnotherTurn) {
			game.setNextPlayer();
		}
		game.getTilePack().cleanUpTurn();
		next();
	}

}
