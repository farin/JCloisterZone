package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.Game;

public class Wagon extends Follower {

	private static final long serialVersionUID = 2585914429763599776L;

	public Wagon(Game game, Player player) {
		super(game, player);
	}

	@Override
	protected void checkDeployment(Feature f) {
		if (f instanceof Tower) {
			throw new IllegalArgumentException("Cannot place wagon on the tower.");
		}
		if (f instanceof Farm) {
			throw new IllegalArgumentException("Cannot place wagon on the farm.");
		}
		super.checkDeployment(f);
	}

}
