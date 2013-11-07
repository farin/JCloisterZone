package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.game.Game;

public class Pig extends Special {

	private static final long serialVersionUID = -6315956811639409025L;

	public Pig(Game game, Player player) {
		super(game, player);
	}

	@Override
	protected void checkDeployment(Feature farm) {
		if (!(farm instanceof Farm)) {
			throw new IllegalArgumentException("Pig must be placed on a farm only.");
		}
		if (!farm.walk(new IsOccupied().with(Follower.class))) {
			throw new IllegalArgumentException("Feature is not occupied by follower.");
		}
		super.checkDeployment(farm);
	}
}
