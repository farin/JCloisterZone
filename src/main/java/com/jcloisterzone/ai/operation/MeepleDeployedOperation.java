package com.jcloisterzone.ai.operation;

import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class MeepleDeployedOperation implements Operation {

	private final Meeple meeple;

	public MeepleDeployedOperation(Meeple meeple) {
		this.meeple = meeple;
	}

	@Override
	public void undo(Game game) {
		meeple.undeploy(false);
	}

}
