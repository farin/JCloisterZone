package com.jcloisterzone.ai.copy;

import java.awt.image.TileObserver;
import java.util.Collections;
import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TilePackFactory;
import com.jcloisterzone.game.Game;

public class TilePackCopy implements TilePack {

	private final TilePack tilePack;
	private Tile currentTileCopy;
	private Game game;

	public TilePackCopy(TilePack tilePack, Game game) {
		this.tilePack = tilePack;
		this.game = game;
	}

	@Override
	public Tile getCurrentTile() {
		if (currentTileCopy == null) {
			//TODO load without parsing ?
			//currentTileCopy = (Tile) originalTile.clone();
			//currentTileCopy.setGame(game);

			Tile originalTile = tilePack.getCurrentTile();
			Expansion tileExpansion = Expansion.valueOfCode(originalTile.getId().substring(0, 2));
			TilePackFactory tilePackFactory = new TilePackFactory();
			tilePackFactory.setGame(game);
			tilePackFactory.setExpansions(Collections.singleton(tileExpansion));
			currentTileCopy = tilePackFactory.createTileForId(originalTile.getId());
		}
		return currentTileCopy;
	}

	@Override
	public int tolalSize() {
		return tilePack.tolalSize();
	}

	@Override
	public boolean isEmpty() {
		return tilePack.isEmpty();
	}

	@Override
	public int size() {
		return tilePack.size();
	}

	@Override
	public boolean isGroupActive(String group) {
		return tilePack.isGroupActive(group);
	}

	@Override
	public Set<String> getGroups() {
		return tilePack.getGroups();
	}

	@Override
	public void cleanUpTurn() {
		currentTileCopy = null;
	}

	@Override
	public int getSizeForEdgePattern(EdgePattern ep) {
		return getSizeForEdgePattern(ep);
	}

	@Override
	public Tile drawTile(int index) {
		throw new UnsupportedOperationException("Tile pack cannot be modified");
	}

	@Override
	public Tile drawTile(String groupId, String tileId) {
		throw new UnsupportedOperationException("Tile pack cannot be modified");
	}

	@Override
	public Tile drawTile(String tileId) {
		throw new UnsupportedOperationException("Tile pack cannot be modified");
	}

	@Override
	public Tile getAbbeyTile() {
		throw new UnsupportedOperationException("Tile pack cannot be modified");
	}

	@Override
	public void activateGroup(String group) {
		throw new UnsupportedOperationException("Tile pack cannot be modified");

	}

	@Override
	public void deactivateGroup(String group) {
		throw new UnsupportedOperationException("Tile pack cannot be modified");

	}



}
