package com.jcloisterzone.feature.visitor.score;

import java.util.Map;

import com.jcloisterzone.TradeResource;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

public class CityScoreContext extends PositionCollectingScoreContext {

	private Map<City, CityScoreContext> cityCache;

	private boolean cathedral, besieged;
	private int pennants;
	private int cityTradeResources[];


	public CityScoreContext(Game game) {
		super(game);
	}

	public Map<City, CityScoreContext> getCityCache() {
		return cityCache;
	}

	public void setCityCache(Map<City, CityScoreContext> cityCache) {
		this.cityCache = cityCache;
	}

	@Override
	public boolean visit(Feature feature) {
		City city = (City) feature;
		pennants += city.getPennants();
		cathedral = cathedral || city.isCathedral();
		besieged = besieged || city.isBesieged();
		TradeResource tr = city.getTradeResource();
		if (tr != null) {
			if (cityTradeResources == null) {
				cityTradeResources = new int[TradeResource.values().length];
			}
			cityTradeResources[tr.ordinal()]++;
		}
		if (cityCache != null) {
			cityCache.put(city, this);
		}
		return super.visit(feature);
	}

	@Override
	public int getPoints() {
		return getPoints(isCompleted());
	}

	@Override
	public int getPoints(boolean completed) {
		int size = getPositions().size();
		if (size <= 2 && getGame().hasRule(CustomRule.TINY_CITY_2_POINTS)) {
			//small city can has pennant! (Abbey and Mayor)
			return size + pennants;
		}
		int pointsPerUnit = 2;
		if (completed) {
			if (cathedral) pointsPerUnit = 3;
		} else {
			if (cathedral) return 0;
			pointsPerUnit = 1;
		}
		if (besieged) {
			pointsPerUnit--;
		}
		return pointsPerUnit * (size + pennants);
	}

	public int[] getCityTradeResources() {
		return cityTradeResources;
	}

	public boolean isCathedral() {
		return cathedral;
	}

	public boolean isBesieged() {
		return besieged;
	}

	public int getPennants() {
		return pennants;
	}



}
