package com.jcloisterzone.board;

import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;


public enum TileSymmetry {

	NONE,
	S2,
	S4;

	static TileSymmetry forTile(Tile tile) {
		TileSymmetry symetry = countBaseSymetry(tile);
		Location river = tile.getRiver();
		if (river != null) {
			if (river == Location.WE || river == Location.NS) {
				if (symetry == TileSymmetry.S4) symetry = TileSymmetry.S2;
			} else {
				symetry = TileSymmetry.NONE;
			}
		}
		return symetry;
	}

	private static TileSymmetry countBaseSymetry(Tile tile) {
		for (Feature piece : tile.getFeatures()) {
			if (piece instanceof Road|| piece instanceof City) {
				Feature opposite = tile.getFeature(piece.getLocation().rev());
				if (opposite == null || ! opposite.getClass().equals(piece.getClass())) {
					return TileSymmetry.NONE;
				}
			}
		}
		for (Feature piece : tile.getFeatures()) {
			if (piece instanceof Road|| piece instanceof City) {
				Feature opposite = tile.getFeature(piece.getLocation().rotateCW(Rotation.R90));
				if (opposite == null || ! opposite.getClass().equals(piece.getClass())) {
					return TileSymmetry.S2;
				}
			}
		}
		return TileSymmetry.S4;
	}

}
