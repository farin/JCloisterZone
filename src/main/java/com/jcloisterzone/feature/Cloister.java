package com.jcloisterzone.feature;


import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.visitor.score.CloisterScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;


public class Cloister extends TileFeature implements Completable {

	private boolean shrine;

	public boolean isShrine() {
		return shrine;
	}

	public void setShrine(boolean shrine) {
		this.shrine = shrine;
	}

	@Override
	public boolean isOpen() {
		Position p = getTile().getPosition();
		return getGame().getBoard().getAdjacentAndDiagonalTiles(p).size() < 8;
	}

	@Override
	public CompletableScoreContext getScoreContext() {
		return new CloisterScoreContext(getGame());
	}

	@Override
	public PointCategory getPointCategory() {
		return PointCategory.CLOISTER;
	}

}
