package com.jcloisterzone.board;

/** When thrown by capability from initTile method, tile is not included in tile pack */
public class RemoveTileException extends Exception {
    //TODO what about declarative definition in xml (but it depends on rules)
    //some removals are now in capability code others declared with <discard> tag -> unify
}
