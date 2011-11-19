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
	public boolean isPieceCompleted() {
		Position p = getTile().getPosition();
		return getGame().getBoard().getAllNeigbourTiles(p).size() == 8;
	}

	@Override
	public boolean isFeatureCompleted() {
		return isPieceCompleted();
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
