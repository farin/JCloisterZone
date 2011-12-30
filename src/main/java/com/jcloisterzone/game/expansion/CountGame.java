package com.jcloisterzone.game.expansion;

import com.jcloisterzone.game.ExpandedGame;

public class CountGame extends ExpandedGame {
	
	@Override
	public void begin() {
		game.getTilePack().activateGroup("count");
	}

}
