package com.jcloisterzone.board;

/**
 * Enumerates the symmetry conditions of a tile.
 */
public enum TileSymmetry {

    /**
     * No symmetry
     */
    NONE,

    /**
     * Opposite edges are equal
     */
    S2,

    /**
     * All edges are equal
     */
    S4
}