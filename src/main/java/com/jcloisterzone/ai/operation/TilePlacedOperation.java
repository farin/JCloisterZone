package com.jcloisterzone.ai.operation;

import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;

public class TilePlacedOperation implements Operation {

	private final Tile tile;

    public TilePlacedOperation(Tile tile) {
		this.tile = tile;
	}

	@Override
	public void undo(Game game) {
		game.getBoard().unmergeFeatures(tile);
		game.getBoard().remove(tile);		
		if (tile.isAbbeyTile()) {
			((DefaultTilePack)game.getTilePack()).addTile(tile, TilePack.INACTIVE_GROUP);
		}
	}
}
