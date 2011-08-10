package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;

public class IsOccupiedVisitor implements FeatureVisitor {

	private Player player;
	private Class<? extends Meeple> clazz;
	private boolean isOccupied = false;

	public IsOccupiedVisitor() {
	}

	public IsOccupiedVisitor(Player player) {
		this.player = player;
	}
	public IsOccupiedVisitor(Class<? extends Meeple> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean visit(Feature feature) {
		if (player != null) {
			isOccupied = feature.isOccupiedBy(player);
		} else if (clazz != null) {
			isOccupied = feature.isOccupiedBy(clazz);
		} else {
			isOccupied = feature.isOccupied();
		}
		return ! isOccupied; //retunr = continue with more fearures
	}

	public boolean isOccupied() {
		return isOccupied;
	}


}
