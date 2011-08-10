package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;

public class DragonPhase extends Phase {

	public DragonPhase(Game game) {
		super(game);
	}

	@Override
	public boolean isActive() {
		return game.hasExpansion(Expansion.PRINCESS_AND_DRAGON);
	}

	@Override
	public void enter() {
		if (getTile().getTrigger() == TileTrigger.DRAGON) {
			PrincessAndDragonGame pd = game.getPrincessAndDragonGame();
			if (pd.getDragonPosition() != null) {
				pd.triggerDragonMove();
				next(DragonMovePhase.class);
				return;
			}
		}
		next();
	}






}
