package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;

public class IsOccupied implements FeatureVisitor<Boolean> {

	private Player player;
	private Class<? extends Meeple> clazz;
	
	private boolean isOccupied = false;
	
	public IsOccupied with(Player player) {
		this.player = player;
		return this;
	}
	
	public IsOccupied with(Class<? extends Meeple> clazz) {
		this.clazz = clazz;
		return this;
	}	

	@Override
	public boolean visit(Feature feature) {
		Meeple m = feature.getMeeple();
		if (m == null) return true;
		if (player != null && m.getPlayer() != player) return true;
		if (clazz != null && ! clazz.isInstance(m)) return true;
		isOccupied = true;
		return false;
	}

	@Override
	public Boolean getResult() {
		return isOccupied;
	}
}
