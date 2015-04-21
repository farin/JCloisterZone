package com.jcloisterzone.board;

import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;


public enum TileSymmetry {

    NONE,
    S2,
    S4;

    static TileSymmetry forTile(Tile tile) {
        if (tile.getFlier() != null) {
            return TileSymmetry.NONE;
        }
        if (tile.getWindRose() != null) {
            return TileSymmetry.NONE;
        }
        TileSymmetry symmetry = countBaseSymmetry(tile);
        Location river = tile.getRiver();
        if (river != null) {
            if (river == Location.WE || river == Location.NS) {
                if (symmetry == TileSymmetry.S4) symmetry = TileSymmetry.S2;
            } else {
                symmetry = TileSymmetry.NONE;
            }
        }
        return symmetry;
    }

    private static TileSymmetry countBaseSymmetry(Tile tile) {
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
