package com.jcloisterzone.game.expansion;

import com.jcloisterzone.Expansion;

public final class RiverGame extends AbstractRiverGame {

	private static final String LAKE_ID = "R1.I.e";


	@Override
	public void begin() {
		if (! getGame().hasExpansion(Expansion.RIVER_II)) {
			super.begin();
			getTilePack().activateGroup("river");
		}
	}

	@Override
	protected String getLakeId() {
		return LAKE_ID;
	}

	@Override
	public void turnCleanUp() {
		if (! getGame().hasExpansion(Expansion.RIVER_II)) {
			super.turnCleanUp();
		}
	}

}
