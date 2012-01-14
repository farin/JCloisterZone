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
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.feature.visitor.IsOccupiedOrCompleted;
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
		Player p = game.getAbbeyAndMayorGame().getWagonPlayer();
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
			Sites wagonMoves = prepareWagonMoves(f);
			if (! wagonMoves.isEmpty()) {
				amGame.setWagonPlayer(player);
				PlayerAction action = new MeepleAction(Wagon.class, wagonMoves);
				game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
				game.getUserInterface().selectAction(Collections.singletonList(action));
				return true;
			}
		}
		return false;
	}
	
	private Sites prepareWagonMoves(Feature source) {		
		return source.walk(new FindUnoccupiedNeighbours());
	}

	private class FindUnoccupiedNeighbours implements FeatureVisitor<Sites> {

		private Sites wagonMoves = new Sites();

		@Override
		public boolean visit(Feature feature) {
			if (feature.getNeighbouring() != null) {
				for(Feature nei : feature.getNeighbouring()) {
					if (nei.walk(new IsOccupiedOrCompleted())) continue;									
					wagonMoves.getOrCreate(feature.getTile().getPosition()).add(nei.getLocation());
				}
			}
			return true;
		}

		public Sites getResult() {
			return wagonMoves;
		}
	}

}
