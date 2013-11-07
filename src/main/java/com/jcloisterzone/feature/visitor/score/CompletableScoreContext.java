package com.jcloisterzone.feature.visitor.score;

import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Completable;


public interface CompletableScoreContext extends ScoreContext {

	boolean isCompleted();
	Set<Position> getPositions(); 

	int getPoints();

	@Override
	public Completable getMasterFeature();

}
