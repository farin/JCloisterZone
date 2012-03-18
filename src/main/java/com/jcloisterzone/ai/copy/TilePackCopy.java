package com.jcloisterzone.ai.copy;

import java.util.Set;

import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;

@Deprecated //TODEL
public class TilePackCopy implements TilePack {

	private final TilePack tilePack;
	//private Tile currentTileCopy;
	private Game game;

	public TilePackCopy(TilePack tilePack, Game game) {
		this.tilePack = tilePack;
		this.game = game;
	}

	@Override
	public Tile getCurrentTile() {
//		if (currentTileCopy == null) {
//			//TODO load without parsing ?
//			//currentTileCopy = (Tile) originalTile.clone();
//			//currentTileCopy.setGame(game);
//
//			Tile originalTile = tilePack.getCurrentTile();
//			Expansion tileExpansion = Expansion.valueOfCode(originalTile.getId().substring(0, 2));
//			TilePackFactory tilePackFactory = new TilePackFactory();
//			tilePackFactory.setGame(game);
//			tilePackFactory.setExpansions(Collections.singleton(tileExpansion));
//			currentTileCopy = tilePackFactory.createTileForId(originalTile.getId());
//		}
//		return currentTileCopy;
		return tilePack.getCurrentTile();
	}

	@Override
	public int totalSize() {
		return tilePack.totalSize();
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
		if (tileId.equals(tilePack.getCurrentTile().getId())) {
			return tilePack.getCurrentTile();
		}
		throw new UnsupportedOperationException("Cannot draw another tile.");
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
