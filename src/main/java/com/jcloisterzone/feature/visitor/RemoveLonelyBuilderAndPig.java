package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;

public class RemoveLonelyBuilderAndPig implements FeatureVisitor {

	Player player;
	Meeple toRemove = null;

	public RemoveLonelyBuilderAndPig(Player player) {
		this.player = player;
	}

	public Meeple getMeepleToRemove() {
		return toRemove;
	}

	@Override
	public boolean visit(Feature feature) {
		Meeple m = feature.getMeeple();
		if (! (m instanceof Follower) || m.getPlayer() != player) {
			return true;
		}
		if (m instanceof Builder || m instanceof Pig) {
			toRemove = m;
			return true;
		} else {
			//another follower exists
			toRemove = null;
			return false;
		}
	}

}