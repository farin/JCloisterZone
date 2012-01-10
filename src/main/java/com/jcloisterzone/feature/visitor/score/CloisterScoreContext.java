package com.jcloisterzone.feature.visitor.score;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Game;

public class CloisterScoreContext implements CompletableScoreContext {

	private int neigbouringTiles;
	private Cloister cloister;
	private Game game;

	public CloisterScoreContext(Game game) {
		this.game = game;
	}

	@Override
	public Cloister getMasterFeature() {
		return cloister;
	}

	@Override
	public int getPoints() {
		return neigbouringTiles + 1;
	}

	@Override
	public Set<Position> getPositions() {
		return Collections.singleton(cloister.getTile().getPosition());
	}

	@Override
	public boolean visit(Feature feature) {
		cloister = (Cloister) feature;
		Position pos = cloister.getTile().getPosition();
		neigbouringTiles = game.getBoard().getAdjacentAndDiagonalTiles(pos).size();
		return true;
	}

	@Override
	public Follower getSampleFollower(Player player) {
		if (cloister.getMeeple().getPlayer() == player) return (Follower) cloister.getMeeple();
		return null;
	}

	@Override
	public Set<Player> getMajorOwners() {
		if (cloister.getMeeple() == null) return Collections.emptySet();
		return Collections.singleton(cloister.getMeeple().getPlayer());
	}

	@Override
	public List<Follower> getFollowers() {
		if (cloister.getMeeple() == null) return Collections.emptyList();
		return Collections.singletonList((Follower) cloister.getMeeple());
	}

	@Override
	public List<Special> getSpecialMeeples() {
		return Collections.emptyList();
	}

	@Override
	public Iterable<Meeple> getMeeples() {
		return Iterables.<Meeple>concat(getFollowers(), getSpecialMeeples());
	}

	@Override
	public boolean isCompleted() {
		return neigbouringTiles == 8;
	}

}
