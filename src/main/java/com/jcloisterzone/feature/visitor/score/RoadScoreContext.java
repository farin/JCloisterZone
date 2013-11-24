package com.jcloisterzone.feature.visitor.score;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Game;

public class RoadScoreContext extends PositionCollectingScoreContext {

	private boolean isInn;

	public RoadScoreContext(Game game) {
		super(game);
	}

	@Override
	public boolean visit(Feature feature) {
		isInn = isInn || ((Road)feature).isInn();
		return super.visit(feature);
	}

	@Override
	public int getPoints() {
		return getPoints(isCompleted());
	}

	@Override
	public int getPoints(boolean completed) {
		int length = getPositions().size();
		if (isInn) {
			return completed ? length * 2 : 0;
		} else {
			return length;
		}
	}

	public boolean isInn() {
		return isInn;
	}


}
