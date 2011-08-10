package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.Map;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.AbbeyAndMayorGame;


public class WagonPhase extends Phase {


	public WagonPhase(Game game) {
		super(game);
	}

	@Override
	public boolean isActive() {
		return game.hasExpansion(Expansion.ABBEY_AND_MAYOR);
	}

	@Override
	public void enter() {
		if (! existsLegalMove()) next();
	}

	@Override
	public void placeNoFigure() {
		enter();
	}

	@Override
	public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
		if (! meepleType.equals(Wagon.class)) {
			logger.error("Illegal figure type.");
			return;
		}
		Meeple m = getActivePlayer().getUndeployedMeeple(Wagon.class);
		m.deploy(getBoard().get(p), loc);
		enter();
	}

	@Override
	public Player getActivePlayer() {
		Player p = game.getAbbeyAndMayorGame().getVagonPlayer();
		return p == null ? game.getTurnPlayer() : p;
	}

	private boolean existsLegalMove() {
		AbbeyAndMayorGame amGame = game.getAbbeyAndMayorGame();
		Map<Player, Feature> rw = amGame.getReturnedWagons();
		while(! rw.isEmpty()) {
			int pi = game.getTurnPlayer().getIndex();
			while(! rw.containsKey(game.getAllPlayers()[pi])) {
				pi++;
				if (pi == game.getAllPlayers().length) pi = 0;
			}
			Player player = game.getAllPlayers()[pi];
			Feature f = rw.remove(player);
			Sites vagonMoves = prepareVagonMoves(f);
			if (! vagonMoves.isEmpty()) {
				amGame.setVagonPlayer(player);
				PlayerAction action = new MeepleAction(Wagon.class, vagonMoves);
				game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
				game.getUserInterface().selectAction(Collections.singletonList(action));
				return true;
			}
		}
		return false;
	}

	class FindUnoccupiedNeighbours implements FeatureVisitor {

		private Sites vagonMoves = new Sites();

		@Override
		public boolean visit(Feature feature) {
			if (feature.getNeighbouring() != null) {
				for(Feature nei : feature.getNeighbouring()) {
					//TODO double walk
					if (nei.isFeatureOccupied()) continue;
					if (nei instanceof Completable && ((Completable) nei).isFeatureCompleted()) continue;
					vagonMoves.getOrCreate(feature.getTile().getPosition()).add(nei.getLocation());
				}
			}
			return true;
		}

		public Sites getVagonMoves() {
			return vagonMoves;
		}

	}

	private Sites prepareVagonMoves(Feature source) {
		FindUnoccupiedNeighbours visitor = new FindUnoccupiedNeighbours();
		source.walk(visitor);
		return visitor.getVagonMoves();
	}
}
