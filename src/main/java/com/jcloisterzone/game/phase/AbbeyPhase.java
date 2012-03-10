package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.AbbeyAndMayorGame;

public class AbbeyPhase extends Phase {

	public AbbeyPhase(Game game) {
		super(game);
	}

	@Override
	public boolean isActive() {
		return game.hasExpansion(Expansion.ABBEY_AND_MAYOR);
	}

	@Override
	public void enter() {
		AbbeyAndMayorGame amGame = game.getAbbeyAndMayorGame();
		if (amGame.hasUnusedAbbey(getActivePlayer()) && ! getBoard().getHoles().isEmpty()) {
			game.getUserInterface().selectAbbeyPlacement(getBoard().getHoles());
		} else {
			next();
		}
	}

	@Override
	public void pass() {
		next();
	}

	@Override
	public void placeTile(Rotation rotation, Position position) {
		AbbeyAndMayorGame amGame = game.getAbbeyAndMayorGame();
		amGame.useAbbey(getActivePlayer());

		Tile nextTile = game.getTilePack().drawTile("inactive", Tile.ABBEY_TILE_ID);
		nextTile.setRotation(rotation);
		getBoard().add(nextTile, position);
		getBoard().mergeFeatures(nextTile);

		game.fireGameEvent().tilePlaced(nextTile);
		next(ActionPhase.class);
	}



}
