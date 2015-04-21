package com.jcloisterzone.feature;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.feature.visitor.score.ScoreContext;

public interface Scoreable extends Feature {

	PointCategory getPointCategory();
	ScoreContext getScoreContext();

}
