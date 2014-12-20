package com.jcloisterzone.feature.score;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;

public interface ScoreAllCallback {

	void scoreCompletableFeature(CompletableScoreContext ctx);
	void scoreFarm(FarmScoreContext ctx, Player player);
	void scoreBarn(FarmScoreContext ctx, Barn meeple);
	void scoreCastle(Meeple meeple, Castle castle);

	CompletableScoreContext getCompletableScoreContext(Completable completable);
	FarmScoreContext getFarmScoreContext(Farm farm);

}

