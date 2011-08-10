package com.jcloisterzone.feature;

import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;

public interface Completable extends Scoreable {

	boolean isPieceCompleted();
	//TODO in fact not use except one Cult call - remove ?
	boolean isFeatureCompleted();

	CompletableScoreContext getScoreContext();


}
