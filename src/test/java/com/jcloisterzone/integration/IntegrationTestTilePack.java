package com.jcloisterzone.integration;

import com.jcloisterzone.board.TileGroup;
import com.jcloisterzone.board.TilePack;

import io.vavr.collection.LinkedHashMap;

public class IntegrationTestTilePack extends TilePack {

    public IntegrationTestTilePack(LinkedHashMap<String, TileGroup> groups) {
        super(groups);
    }

    public IntegrationTestTilePack setGroups(LinkedHashMap<String, TileGroup> groups) {
        if (getGroups() == groups) return this;
        return new IntegrationTestTilePack(groups);
    }

}
