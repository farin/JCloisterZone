package com.jcloisterzone.feature.score;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

/**
 * Sort farm for correct 1st edition scoring.
 */
public class ScoreAllFeatureFinder {

	private Set<Follower> alreadyRated = Sets.newHashSet();
	private List<FarmScoreContext> farmContexts = Lists.newArrayList();
	private Map<City, CityScoreContext> cityCache = Maps.newHashMap();
	private Map<Player, Set<City>> scoredCities;

	public void scoreAll(Game game, ScoreAllCallback callback) {
		if (game.hasRule(CustomRule.FARM_CITY_SCORED_ONCE)) {
			scoredCities = Maps.newHashMap();
			for(Player p : game.getAllPlayers()) {
				scoredCities.put(p, Sets.<City>newHashSet());
			}
		}
		for(Meeple m : game.getDeployedMeeples()) {
			if (!(m instanceof Follower)) continue;
			Feature f = m.getFeature();				
			
			if (f instanceof Castle) {
				callback.scoreCastle(m, (Castle) f);
				continue;
			}
			
			if (!(f instanceof Completable)) continue;
			if (alreadyRated.contains(m)) continue;	
			
			Completable completable = (Completable) f;
			CompletableScoreContext ctx = callback.getCompletableScoreContext(completable);
			if (ctx instanceof CityScoreContext) {
				((CityScoreContext) ctx).setCityCache(cityCache);
			}
			completable.walk(ctx);
			alreadyRated.addAll(ctx.getFollowers());
			callback.scoreCompletableFeature(ctx);
		}
		for(Meeple m : game.getDeployedMeeples()) {
			if (! (m instanceof Follower) && !(m instanceof Barn)) continue;
			Feature f = m.getFeature();
			if (! (f instanceof Farm)) continue;
			if (alreadyRated.contains(m)) continue;

			Farm farm = (Farm) f;
			FarmScoreContext ctx = callback.getFarmScoreContext(farm);
			ctx.setCityCache(cityCache);
			if (scoredCities != null) {
				ctx.setScoredCities(scoredCities);
			}
			farm.walk(ctx);
			alreadyRated.addAll(ctx.getFollowers());
			farmContexts.add(ctx);
		}
		for(Player p : game.getAllPlayers()) {
			Collections.sort(farmContexts, new FarmPoitsPerCityComparator(p));
			for(FarmScoreContext ctx : farmContexts) {
				if (ctx.getMajorOwners().contains(p)) {
					callback.scoreFarm(ctx, p);
				}
			}
		}
		for(FarmScoreContext ctx : farmContexts) {
			for(Special m : ctx.getSpecialMeeples()) {
				if (m instanceof Barn) {
					callback.scoreBarn(ctx, (Barn) m);
				}
			}
		}
	}

	private static class FarmPoitsPerCityComparator implements Comparator<FarmScoreContext> {

		private final Player player;

		public FarmPoitsPerCityComparator(Player player) {
			this.player = player;
		}

		@Override
		public int compare(FarmScoreContext o1, FarmScoreContext o2) {
			//reverse order according to city points
			return o2.getPointsPerCity(player) - o1.getPointsPerCity(player);
		}

	}

}
