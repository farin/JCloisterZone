package com.jcloisterzone.feature.score;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;

public interface ScoreAllCallback {

	void scoreCompletableFeature(CompletableScoreContext ctx);
	void scoreFarm(FarmScoreContext ctx, Player player);
	void scoreBarn(FarmScoreContext ctx, Barn meeple);

	CompletableScoreContext getCompletableScoreContext(Completable completable);
	FarmScoreContext getFarmScoreContext(Farm farm);

}

