package com.jcloisterzone.feature;

import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;

public interface Completable extends Scoreable {

	boolean isOpen();
	CompletableScoreContext getScoreContext();

}
