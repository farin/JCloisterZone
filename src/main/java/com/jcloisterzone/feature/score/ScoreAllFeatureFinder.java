package com.jcloisterzone.feature.score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Game;

/**
 * Sort farm for correct 1st edition scoring.
 */
public class ScoreAllFeatureFinder {

    private Set<Meeple> alreadyRated = new HashSet<>();
    private Map<City, CityScoreContext> cityCache = new HashMap<>();

    private void scoreCompletable(Completable completable, ScoreAllCallback callback) {
        CompletableScoreContext ctx = callback.getCompletableScoreContext(completable);
        if (ctx instanceof CityScoreContext) {
            ((CityScoreContext) ctx).setCityCache(cityCache);
        }
        completable.walk(ctx);
        alreadyRated.addAll(ctx.getFollowers());
        callback.scoreCompletableFeature(ctx);
    }

    private void scoreFarm(Farm farm, ScoreAllCallback callback) {
        FarmScoreContext ctx = callback.getFarmScoreContext(farm);
        ctx.setCityCache(cityCache);
        farm.walk(ctx);
        alreadyRated.addAll(ctx.getFollowers());
        alreadyRated.addAll(ctx.getSpecialMeeples()); //farm can contains Barn!

        for (Special sp : ctx.getSpecialMeeples()) {
            if (sp instanceof Barn) {
                callback.scoreBarn(ctx, (Barn) sp);
            }
        }
        for (Player p : ctx.getMajorOwners()) {
            callback.scoreFarm(ctx, p);
        }
    }

    public void scoreAll(Game game, ScoreAllCallback callback) {
        //first score non-farm features to fill city cache
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower)) continue;
            Feature f = m.getFeature();

            if (f instanceof Castle) {
                callback.scoreCastle(m, (Castle) f);
                continue;
            }
            if (f instanceof Completable) {
                if (alreadyRated.contains(m)) continue;
                scoreCompletable((Completable) f, callback);
            }
        }

        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m instanceof Follower) && !(m instanceof Barn)) continue;
            Feature f = m.getFeature();

            if (f instanceof Farm) {
                if (alreadyRated.contains(m)) continue;
                scoreFarm((Farm) f, callback);
            }
        }
    }
}
