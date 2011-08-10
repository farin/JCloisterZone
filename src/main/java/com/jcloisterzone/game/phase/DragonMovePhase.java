package com.jcloisterzone.game.phase;

import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;


public class DragonMovePhase extends Phase {

	public DragonMovePhase(Game game) {
		super(game);
	}

	@Override
	public boolean isActive() {
		return game.hasExpansion(Expansion.PRINCESS_AND_DRAGON);
	}

	@Override
	public Player getActivePlayer() {
		PrincessAndDragonGame pdGame = game.getPrincessAndDragonGame();
		return game.getPlayer(pdGame.getDragonPlayer());
	}

	@Override
	public void enter() {
		PrincessAndDragonGame pd = game.getPrincessAndDragonGame();
		if (pd.getDragonMovesLeft() > 0) {
			Set<Position> moves = pd.getAvailDragonMoves();
			if (! moves.isEmpty()) {
				game.getUserInterface().selectDragonMove(moves, pd.getDragonMovesLeft());
				return;
			}
		}
		pd.endDragonMove();
		next();
	}

	@Override
	public void moveDragon(Position p) {
		PrincessAndDragonGame pd = game.getPrincessAndDragonGame();
		if (! pd.getAvailDragonMoves().contains(p)) {
			throw new IllegalArgumentException("Ïnvalid dragon move.");
		}
		pd.getDragonVisitedTiles().add(p);
		pd.setDragonPosition(p);
		pd.nextDragonPlayer();
		for(Meeple m : game.getDeployedMeeples()) {
			if (m.getPosition().equals(p)) {
				m.undeploy();
			}
		}
		game.fireGameEvent().dragonMoved(p);
		game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
		next(DragonMovePhase.class);
	}


}
