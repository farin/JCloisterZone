package com.jcloisterzone.game.expansion;

import org.w3c.dom.Element;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.ExpandedGame;

public class CountGame extends ExpandedGame {
	
	@Override
	public void begin() {
		game.getTilePack().activateGroup("count");
	}
	
	@Override
	public void initTile(Tile tile, Element xml) {
		if (tile.getId().startsWith(Expansion.COUNT.getCode())) {
			tile.setForbidden(true);
		}
	}
}
