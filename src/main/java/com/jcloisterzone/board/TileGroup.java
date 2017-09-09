package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import com.jcloisterzone.Immutable;

import io.vavr.collection.Vector;

/**
 * Represents a group of tiles. This abstraction is useful to separate groups of tiles that can only be used when some
 * conditions are met; dragon tiles and lake tiles are examples of this. This is convenient because a {@code TileGroup}
 * can be deactivated until the condition is met, so as not to draw these tiles until they are usable.
 */
@Immutable
public class TileGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Vector<Tile> tiles;
    private final boolean active;

    /** Name of group which should be activate when this is depleted.*/
    private final String successiveGroup;

    /**
     * Instantiates a new {@code TileGroup}.
     *
     * @param name   a name for the group
     * @param tiles  the tiles in the group
     * @param active a flag indicating whether this group is active
     */
    public TileGroup(String name, Vector<Tile> tiles, boolean active) {
        this(name, tiles, active, null);
    }

    /**
     * Instantiates a new {@code TileGroup}.
     *
     * @param name            a name for the group
     * @param tiles           the tiles in the group
     * @param active          a flag indicating whether this group is active
     * @param successiveGroup the name of the group that should be activated when this is depleted
     */
    public TileGroup(String name, Vector<Tile> tiles, boolean active, String successiveGroup) {
        this.name = name;
        this.tiles = tiles;
        this.active = active;
        this.successiveGroup = successiveGroup;
    }

    /**
     * Gets the name of this group.
     *
     * @return the name of this group
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the tiles of this group.
     *
     * @return the tiles of this group
     */
    public Vector<Tile> getTiles() {
        return tiles;
    }

    /**
     * Sets the tiles of this group.
     *
     * @param tiles the tiles to set
     * @return a new instance with the tiles set
     */
    public TileGroup setTiles(Vector<Tile> tiles) {
        if (this.tiles == tiles) return this;
        return new TileGroup(name, tiles, active, successiveGroup);
    }

    /**
     * Applies a function {@code fn} to all tiles in this group and returns a new instance where each tile is replaced
     * by the output of the application of {@code fn} to that tile.
     *
     * @param fn the function to apply
     * @return a new instance with the tiles replaced
     */
    public TileGroup mapTiles(Function<Vector<Tile>, Vector<Tile>> fn) {
        return setTiles(fn.apply(tiles));
    }

    /**
     * Checks if this group is active.
     *
     * @return {@code true} if this group is active, {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the flag indicating if this group is active.
     *
     * @param active the new value for the flag
     * @return a new instance with the active flag set
     */
    public TileGroup setActive(boolean active) {
        if (this.active == active) return this;
        return new TileGroup(name, tiles, active, successiveGroup);
    }

    /**
     * Gets the group to use after this is depleted.
     *
     * @return the group to use after this is depleted
     */
    public String getSuccessiveGroup() {
        return successiveGroup;
    }

    /**
     * Sets the group to use after this is depleted.
     *
     * @param successiveGroup the successive group
     * @return a new instance where the successive group is replaced
     */
    public TileGroup setSuccessiveGroup(String successiveGroup) {
        if (Objects.equals(this.successiveGroup, successiveGroup)) return this;
        return new TileGroup(name, tiles, active, successiveGroup);
    }

    /**
     * Returns the number of tiles in this group.
     *
     * @return the number of tiles in this group
     */
    public int size() {
        return tiles.size();
    }

    /**
     * Checks if this group is empty.
     *
     * @return {@code true} if this group is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

}
