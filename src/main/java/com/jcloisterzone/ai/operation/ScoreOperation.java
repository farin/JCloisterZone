package com.jcloisterzone.ai.operation;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.game.Game;

public class ScoreOperation implements Operation {

	private Player player;
	private int points;

	public ScoreOperation(Player player, int points) {
		this.player = player;
		this.points = points;
	}

	@Override
	public void undo(Game game) {
		//TODO is category neeeded
		//hack using dummy category for now
		player.addPoints(-points, PointCategory.TOWER_RANSOM);
	}

}
