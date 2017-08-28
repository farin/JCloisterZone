package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Immutable;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.control.Option;

@Immutable
public class TilePack implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final LinkedHashMap<String, TileGroup> groups;

    public TilePack(LinkedHashMap<String, TileGroup> groups) {
        this.groups = groups;
    }

    public LinkedHashMap<String, TileGroup> getGroups() {
        return groups;
    }

    public TilePack setGroups(LinkedHashMap<String, TileGroup> groups) {
        if (this.groups == groups) return this;
        return new TilePack(groups);
    }

    private Stream<TileGroup> getActiveGroups() {
        return Stream.ofAll(groups.values()).filter(TileGroup::isActive);
    }

    private Stream<TileDefinition> getActiveTiles() {
        return getActiveGroups().flatMap(TileGroup::getTiles);
    }

    public int totalSize() {
        return Stream.ofAll(groups.values()).map(TileGroup::size).sum().intValue();
    }

    public int size() {
        return getActiveGroups().map(TileGroup::size).sum().intValue();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean hasGroup(String name) {
        return groups.containsKey(name);
    }

    public TileGroup getGroup(String name) {
        return groups.get(name).getOrNull();
    }

    public int getGroupSize(String groupId) {
        return groups.get(groupId).map(g -> g.size()).getOrElse(0);
    }

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

    public Tuple2<TileDefinition, TilePack> drawTile(int index) {
        for (TileGroup group : getActiveGroups()) {
            if (index < group.size()) {
                Vector<TileDefinition> tiles = group.getTiles();
                TileDefinition tile = tiles.get(index);
                group = group.setTiles(tiles.removeAt(index));
                return new Tuple2<>(tile, updateGroup(group));
            } else {
                index -= group.size();
            }
        }
        throw new IllegalArgumentException();
    }

    public Tuple2<TileDefinition, TilePack> drawTile(String groupName, String tileId) {
        Predicate<TileDefinition> matchesId = t -> t.getId().equals(tileId);
        TileGroup group = groups.get(groupName)
            .getOrElseThrow(() -> new IllegalArgumentException());
        TileDefinition tile = group.getTiles().find(matchesId)
            .getOrElseThrow(() -> new IllegalArgumentException());
        TilePack pack = updateGroup(group.mapTiles(tiles -> tiles.removeFirst(matchesId)));
        return new Tuple2<>(tile, pack);
    }


    public Tuple2<TileDefinition, TilePack> drawTile(String tileId) {
        for (TileGroup group: getActiveGroups()) {
            try {
                return drawTile(group.getName(), tileId);
            } catch (IllegalArgumentException e) {
                //pass
            }
        }
        throw new IllegalArgumentException("Tile pack does not contain active " + tileId);
    }

    public TilePack removeTilesById(String tileId) {
        return setGroups(groups.mapValues(g ->
            g.mapTiles(tiles -> tiles.filter(tile -> !tile.getId().equals(tileId)))
        ));
    }

    public TilePack activateGroup(String groupName) {
        TileGroup group = groups.get(groupName).get();
        if (group.isActive()) return this;
        return setGroups(groups.put(groupName, group.setActive(true)));
    }

    public TilePack deactivateGroup(String groupName) {
        TileGroup group = groups.get(groupName).get();
        if (!group.isActive()) return this;
        return setGroups(groups.put(groupName, group.setActive(false)));
    }

    public int getSizeForEdgePattern(EdgePattern edgePattern) {
        return getActiveTiles()
            .filter(tile -> edgePattern.isMatchingAnyRotation(tile.getEdgePattern()))
            .size();
    }

    public Option<TileDefinition> findTile(String tileId) {
        Predicate<TileDefinition> pred = t -> t.getId().equals(tileId);
        for (TileGroup group : groups.values()) {
            Option<TileDefinition> res = group.getTiles().find(pred);
            if (!res.isEmpty()) return res;
        }
        return Option.none();
    }

    @Override
    public String toString() {
        return String.format("%s/%s", size(), totalSize());
    }
}
