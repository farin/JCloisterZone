package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.game.RandomGenerator;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.control.Option;

/**
 * Represents a stack of tiles that can be drawn. It handles active/unactivated tiles and only draws from active ones.
 */
@Immutable
public class TilePack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The logger.
     */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final LinkedHashMap<String, TileGroup> groups;

    private final int hiddenUnderHills;

    /**
     * Instantiates a new {@code TilePack}.
     *
     * @param groups the groups making up this pack
     */
    public TilePack(LinkedHashMap<String, TileGroup> groups, int hiddenUnderHills) {
        this.groups = groups;
        this.hiddenUnderHills = hiddenUnderHills;
    }

    /**
     * Gets the groups making up this pack.
     *
     * @return the groups making up this pack
     */
    public LinkedHashMap<String, TileGroup> getGroups() {
        return groups;
    }

    /**
     * Gets number of tiles secretly put face-down under the hill
     *
     * @return number of tiles
     */
    public int getHiddenUnderHills() {
        return hiddenUnderHills;
    }

    /**
     * Sets the groups making up this pack.
     *
     * @param groups the groups
     * @return a new instance with the groups set
     */
    public TilePack setGroups(LinkedHashMap<String, TileGroup> groups) {
        if (this.groups == groups) return this;
        return new TilePack(groups, hiddenUnderHills);
    }

    /**
     * Sets the number of tiles hidden under hills
     *
     * @param hiddenUnderHills tile count
     * @return a new instance with the count set
     */
    public TilePack setHiddenUnderHills(int hiddenUnderHills) {
        if (this.hiddenUnderHills == hiddenUnderHills) return this;
        return new TilePack(groups, hiddenUnderHills);
    }

    private Stream<TileGroup> getActiveGroups() {
        return Stream.ofAll(groups.values()).filter(TileGroup::isActive);
    }

    private Stream<Tile> getActiveTiles() {
        return getActiveGroups().flatMap(TileGroup::getTiles);
    }

    /**
     * Returns the total size of this pack, including active and non-active groups.
     *
     * @return the total size of this pack
     */
    public int totalSize() {
        return Stream.ofAll(groups.values()).map(TileGroup::size).sum().intValue() - hiddenUnderHills;
    }

    /**
     * Returns the size of this pack, including only active groups.
     *
     * @return the size of this pack
     */
    public int size() {
        return getActiveGroups().map(TileGroup::size).sum().intValue() - hiddenUnderHills;
    }

    /**
     * Checks if this pack is empty.
     *
     * @return {@code true} if this pack is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() - hiddenUnderHills <= 0;
    }

    /**
     * Size used for random when tile is drawn.
     */
    protected int getInternalSize() {
        return size() + hiddenUnderHills;
    }

    /**
     * Check if this pack contains a group named {@code name}.
     *
     * @param name the name of interest
     * @return {@code true} if this pack contains a group named {@code name}, {@code false} otherwise
     */
    public boolean hasGroup(String name) {
        return groups.containsKey(name);
    }

    /**
     * Gets the group named {@code name}.
     *
     * @param name the name of interest
     * @return the group named {@code name} if it exists, {@code null} otherwise
     */
    public TileGroup getGroup(String name) {
        return groups.get(name).getOrNull();
    }

    /**
     * Gets the size of the group named {@code name}.
     *
     * @param name the name of interest
     * @return the size of the group named {@code name} if it exists, {@code 0} otherwise
     */
    public int getGroupSize(String name) {
        return groups.get(name).map(TileGroup::size).getOrElse(0);
    }

    /**
     * Applies a function to the group named {@code name}.
     *
     * @param name   the name of interest
     * @param mapper the function to apply to the group
     * @return a new instance with the group named {@code name} replaced by the output of {@code mapper}
     */
    public TilePack mapGroup(String name, Function<TileGroup, TileGroup> mapper) {
        return updateGroup(mapper.apply(getGroup(name)));
    }

    private TilePack updateGroup(TileGroup group) {
        if (group.isEmpty()) {
            TilePack pack = setGroups(groups.remove(group.getName()));
            String succ = group.getSuccessiveGroup();
            if (succ != null && pack.hasGroup(succ)) {
                pack = pack.activateGroup(succ);
            }
            return pack;
        } else {
            return setGroups(groups.put(group.getName(), group));
        }
    }

    /**
     * Draws random tile  {@code index}.
     *
     * @param radom random number generator
     * @return a tuple containing both the tile drawn and the tile pack it belongs to
     * @throws IllegalArgumentException if {@code index} is not strictly less than the size of the pack
     */
    public Tuple2<Tile, TilePack> drawTile(RandomGenerator random) {
        int index = random.nextInt(getInternalSize());
        for (TileGroup group : getActiveGroups()) {
            if (index < group.size()) {
                Vector<Tile> tiles = group.getTiles();
                Tile tile = tiles.get(index);
                group = group.setTiles(tiles.removeAt(index));
                return new Tuple2<>(tile, updateGroup(group));
            } else {
                index -= group.size();
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Draws the tile with id {@code tileId} in group {@code groupName}.
     *
     * @param groupName the group name
     * @param tileId    the tile id
     * @return a tuple containing both the tile drawn and the tile pack it belongs to
     * @throws IllegalArgumentException if there is no group named {@code groupName} or if the group has no tile with id
     * {@code tileId}
     */
    public Tuple2<Tile, TilePack> drawTile(String groupName, String tileId) {
        Predicate<Tile> matchesId = t -> t.getId().equals(tileId);
        TileGroup group = groups.get(groupName)
            .getOrElseThrow(IllegalArgumentException::new);
        Tile tile = group.getTiles().find(matchesId)
            .getOrElseThrow(IllegalArgumentException::new);
        TilePack pack = updateGroup(group.mapTiles(tiles -> tiles.removeFirst(matchesId)));
        return new Tuple2<>(tile, pack);
    }

    /**
     * Draws the tile with id {@code tileId} in group {@code groupName}.
     *
     * @param tileId    the tile id
     * @return a tuple containing both the tile drawn and the tile pack it belongs to
     * @throws IllegalArgumentException if there is no no tile with id {@code tileId}
     */
    public Tuple2<Tile, TilePack> drawTile(String tileId) {
        for (TileGroup group: getActiveGroups()) {
            try {
                return drawTile(group.getName(), tileId);
            } catch (IllegalArgumentException e) {
                //pass
            }
        }
        throw new IllegalArgumentException("Tile pack does not contain active " + tileId);
    }

    /**
     * Removes the tile with id {@code tileId}.
     *
     * @param tileId the tile id
     * @return a new instance with the tile removed
     */
    public TilePack removeTilesById(String tileId) {
        return setGroups(groups.mapValues(g ->
            g.mapTiles(tiles -> tiles.filter(tile -> !tile.getId().equals(tileId)))
        ));
    }

    /**
     * Activates the group named {@code groupName}.
     *
     * @param groupName the group name
     * @return a new instance with the group activated
     */
    public TilePack activateGroup(String groupName) {
        TileGroup group = groups.get(groupName).getOrNull();
        if (group == null || group.isActive()) return this;
        return setGroups(groups.put(groupName, group.setActive(true)));
    }

    /**
     * Deactivates the group named {@code groupName}.
     *
     * @param groupName the group name
     * @return a new instance with the group deactivated
     */
    public TilePack deactivateGroup(String groupName) {
        TileGroup group = groups.get(groupName).get();
        if (!group.isActive()) return this;
        return setGroups(groups.put(groupName, group.setActive(false)));
    }

    /**
     * Gets the number of tiles matching the given edge pattern.
     *
     * @param edgePattern the edge pattern to match
     * @return the number of matching tiles
     */
    public int getSizeForEdgePattern(EdgePattern edgePattern) {
        return getActiveTiles()
            .filter(tile -> edgePattern.isMatchingAnyRotation(tile.getEdgePattern()))
            .size();
    }

    /**
     * Finds the tile with id {@code tileId}.
     *
     * @param tileId the tile id
     * @return {@code Some<TileDefinition>} if a tile with id {@code tileId} is found, {@code None} otherwise
     */
    public Option<Tile> findTile(String tileId) {
        Predicate<Tile> pred = t -> t.getId().equals(tileId);
        for (TileGroup group : groups.values()) {
            Option<Tile> res = group.getTiles().find(pred);
            if (!res.isEmpty()) return res;
        }
        return Option.none();
    }

    @Override
    public String toString() {
        return String.format("%s/%s", size(), totalSize());
    }
}
