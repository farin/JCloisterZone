package com.jcloisterzone.ai.operation;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;

public class TilePlacedOperation implements Operation {

	private final Tile tile;

    public TilePlacedOperation(Tile tile) {
		this.tile = tile;
	}

	@Override
	public void undo(Game game) {
		game.getBoard().remove(tile.getPosition());
		//TODO undo register tower here ?
	}

}
