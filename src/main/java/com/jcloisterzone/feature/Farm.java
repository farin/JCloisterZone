package com.jcloisterzone.feature;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;

public class Farm extends MultiTileFeature {

	protected City[] adjoiningCities;
	protected boolean pigHerd;


	public City[] getAdjoiningCities() {
		return adjoiningCities;
	}

	public void setAdjoiningCities(City[] adjoiningCities) {
		this.adjoiningCities = adjoiningCities;
	}

	public boolean isPigHerd() {
		return pigHerd;
	}

	public void setPigHerd(boolean pigHerd) {
		this.pigHerd = pigHerd;
	}

	@Override
	protected Location[] getSides() {
		return Location.sidesFarm();
	}

	@Override
	public PointCategory getPointCategory() {
		return PointCategory.FARM;
	}

	@Override
	public FarmScoreContext getScoreContext() {
		return new FarmScoreContext(getGame());
	}


}
