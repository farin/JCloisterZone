package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;

import io.vavr.collection.Vector;

@Immutable
public class TileGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Vector<TileDefinition> tiles;
    private final boolean active;

    /** Name of group which should be activate when this is depleted.*/
    private final String successiveGroup;

    public TileGroup(String name, Vector<TileDefinition> tiles, boolean active) {
        this(name, tiles, active, null);
    }

    public TileGroup(String name, Vector<TileDefinition> tiles, boolean active, String successiveGroup) {
        this.name = name;
        this.tiles = tiles;
        this.active = active;
        this.successiveGroup = successiveGroup;
    }

    public String getName() {
        return name;
    }

    public Vector<TileDefinition> getTiles() {
        return tiles;
    }

    public TileGroup setTiles(Vector<TileDefinition> tiles) {
        if (this.tiles == tiles) return this;
        return new TileGroup(name, tiles, active, successiveGroup);
    }

    public TileGroup mapTiles(Function<Vector<TileDefinition>, Vector<TileDefinition>> fn) {
        return setTiles(fn.apply(tiles));
    }

    public boolean isActive() {
        return active;
    }

    public TileGroup setActive(boolean active) {
        if (this.active == active) return this;
        return new TileGroup(name, tiles, active, successiveGroup);
    }

    public String getSuccessiveGroup() {
        return successiveGroup;
    }

    public TileGroup setSuccesiveGroup(String successiveGroup) {
        if (this.successiveGroup == successiveGroup) return this;
        return new TileGroup(name, tiles, active, successiveGroup);
    }

    public int size() {
        return tiles.size();
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

}
