package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.game.Game;

public class NeutralFigure extends Figure {

	private static final long serialVersionUID = 3458278495952412845L;

	public NeutralFigure(Game game) {
		super(game);
	}

	public void deploy(FeaturePointer at) {
		FeaturePointer origin = getFeaturePointer();
		setFeaturePointer(at);
        game.post(new NeutralFigureMoveEvent(game.getActivePlayer(), this, origin, at));
	}

	public void undeploy() {
		deploy(null);
	}

}
