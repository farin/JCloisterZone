package com.jcloisterzone.feature.visitor.score;

import com.jcloisterzone.feature.Completable;


public interface CompletableScoreContext extends ScoreContext {

	public boolean isCompleted();

	int getPoints();

	@Override
	public Completable getMasterFeature();

}
