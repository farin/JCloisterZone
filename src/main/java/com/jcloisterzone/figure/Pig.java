package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public class Pig extends Special {

	private static final long serialVersionUID = -6315956811639409025L;

	public Pig(Game game, Player player) {
		super(game, player);
	}

	@Override
	protected void checkDeployment(Feature piece) {
		if (! (piece instanceof Farm)) {
			throw new IllegalArgumentException("Pig must be placed on a farm only.");
		}
		if (! piece.isFeatureOccupied()) {
			throw new IllegalArgumentException("Feature is not occupied.");
		}
		super.checkDeployment(piece);

	}


}
