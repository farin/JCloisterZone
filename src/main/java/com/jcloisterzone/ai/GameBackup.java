package com.jcloisterzone.ai;

import com.jcloisterzone.ai.copy.CopyGamePhase;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.Phase;

public class GameBackup {

	private final Game game;
	private Snapshot snaphost;

	public GameBackup(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	public Snapshot getSnaphost() {
		if (snaphost == null) {
			snaphost = new Snapshot(game, 0);
		}
		return snaphost;
	}

	public Game copy() {
		Game gameCopy = getSnaphost().asGame();
		gameCopy.setConfig(game.getConfig());
		gameCopy.addGameListener(new GameEventAdapter());
		Phase phase = new CopyGamePhase(gameCopy, getSnaphost(), game.getTilePack());
		gameCopy.getPhases().put(phase.getClass(), phase);
		gameCopy.setPhase(phase);
		phase.startGame();
		return gameCopy;
	}

}