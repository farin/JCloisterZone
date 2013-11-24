package com.jcloisterzone.ai;

import com.jcloisterzone.feature.visitor.score.ScoreContext;

public interface AiScoreContext extends ScoreContext {

	boolean isValid();
	void setValid(boolean valid);
	
}
