package com.jcloisterzone.feature;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;

public class City extends CompletableFeature {

	private int pennants = 0;
	private TradeResource tradeResource;
	private boolean besieged = false;
	private boolean cathedral = false;
	private boolean pricenss;


	public TradeResource getTradeResource() {
		return tradeResource;
	}
	public void setTradeResource(TradeResource tradeResource) {
		this.tradeResource = tradeResource;
	}
	public int getPennants() {
		return pennants;
	}
	public void setPennants(int pennants) {
		this.pennants = pennants;
	}

	public boolean isBesieged() {
		return besieged;
	}
	public void setBesieged(boolean besieged) {
		this.besieged = besieged;
	}
	public boolean isCathedral() {
		return cathedral;
	}
	public void setCathedral(boolean cathedral) {
		this.cathedral = cathedral;
	}
	public boolean isPricenss() {
		return pricenss;
	}
	public void setPricenss(boolean pricenss) {
		this.pricenss = pricenss;
	}

	@Override
	public CityScoreContext getScoreContext() {
		return new CityScoreContext(getGame());
	}


	@Override
	public PointCategory getPointCategory() {
		return PointCategory.CITY;
	}




}
