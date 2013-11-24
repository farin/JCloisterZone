package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.game.Game;

public class Mayor extends Follower {

	private static final long serialVersionUID = -7602411772187519451L;

	public Mayor(Game game, Player player) {
		super(game, player);
	}

	static class PennatsCountingVisitor implements FeatureVisitor<Integer> {
		int pennats = 0;

		@Override
		public boolean visit(Feature feature) {
			City c = (City) feature;
			pennats += c.getPennants();
			return true;
		}

		@Override
		public Integer getResult() {			
			return pennats;
		}
	}

	@Override
	public int getPower() {
		//TODO not effective - city is walked twice during scoring
		return getFeature().walk(new PennatsCountingVisitor());
	}

	@Override
	protected void checkDeployment(Feature f) {
		if (!(f instanceof City)) {
			throw new IllegalArgumentException("Mayor must be placed in city only.");
		}
		super.checkDeployment(f);
	}

}
