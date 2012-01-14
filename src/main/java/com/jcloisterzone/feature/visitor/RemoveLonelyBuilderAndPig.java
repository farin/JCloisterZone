package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Special;

public class RemoveLonelyBuilderAndPig implements FeatureVisitor<Special> {

	Player player;
	Special toRemove = null;

	public RemoveLonelyBuilderAndPig(Player player) {
		this.player = player;
	}

	@Override
	public boolean visit(Feature feature) {
		Meeple m = feature.getMeeple();
		if (m == null || m.getPlayer() != player) {
			return true;
		}
		if (m instanceof Builder || m instanceof Pig) {
			toRemove = (Special) m;
			return true;
		}
		if (m instanceof Follower) {
			//another follower exists
			toRemove = null;
			return false;
		}
		return true; //some special case like Barn
	}

	@Deprecated
	public Meeple getMeepleToRemove() {
		return toRemove;
	}
	
	@Override
	public Special getResult() {	
		return toRemove;
	}


}