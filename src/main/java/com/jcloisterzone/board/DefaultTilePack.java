package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTilePack implements TilePack {

    static class TileGroup {
        final ArrayList<Tile> tiles = new ArrayList<>();
        TileGroupState state = TileGroupState.WAITING;
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, TileGroup> groups = new HashMap<>();
    private Map<EdgePattern, Integer> edgePatterns = new HashMap<>();


    public DefaultTilePack() {
        TileGroup inactive = new TileGroup();
        inactive.state = TileGroupState.RETIRED;
        groups.put(INACTIVE_GROUP, inactive);
    }

    @Override
    public int totalSize() {
        int n = 0;
        for (TileGroup group: groups.values()) {
            if (group.state != TileGroupState.RETIRED) {
                n += group.tiles.size();
            }
        }
        return n;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        int n = 0;
        for (TileGroup group: groups.values()) {
            if (group.state == TileGroupState.ACTIVE) {
                n += group.tiles.size();
            }
        }
        return n;
    }

    @Override
    public Tile drawTile(int index) {
        for (Entry<String,TileGroup> entry: groups.entrySet()) {
            TileGroup group = entry.getValue();
            if (group.state != TileGroupState.ACTIVE) continue;
            ArrayList<Tile> tiles = group.tiles;
            if (index < tiles.size()) {
                Tile currentTile = tiles.remove(index);
                decreaseSideMaskCounter(currentTile, entry.getKey());
                return currentTile;
            } else {
                index -= tiles.size();
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private void increaseSideMaskCounter(Tile tile, String groupId) {
        if (!INACTIVE_GROUP.equals(groupId) && tile.getPosition() == null) {
            Integer countForSideMask = edgePatterns.get(tile.getEdgePattern());
            if (countForSideMask == null) {
                edgePatterns.put(tile.getEdgePattern(), 1);
            } else {
                edgePatterns.put(tile.getEdgePattern(), countForSideMask + 1);
            }
        }
    }

    private void decreaseSideMaskCounter(Tile tile, String groupId) {
        if (tile == null || groupId.equals(INACTIVE_GROUP)) return;
        Integer count = edgePatterns.get(tile.getEdgePattern());
        if (count == null) {
            logger.error("Inconsistent edge mask statistics. Cannot decrease: " + tile.getEdgePattern().toString());
            return;
        }
        if (count == 1) {
            edgePatterns.remove(tile.getEdgePattern());
        } else {
            edgePatterns.put(tile.getEdgePattern(), count - 1);
        }
    }

    @Override
    public Tile drawTile(String groupId, String tileId) {
        ArrayList<Tile> tiles = groups.get(groupId).tiles;
        Iterator<Tile> i = tiles.iterator();
        while(i.hasNext()) {
            Tile tile = i.next();
            if (tile.getId().equals(tileId)) {
                i.remove();
                decreaseSideMaskCounter(tile, groupId);
                return tile;
            }
        }
        return null;
    }

    @Override
    public Tile drawTile(String tileId) {
        for (String groupId: groups.keySet()) {
            Tile tile = drawTile(groupId, tileId);
            if (tile != null) return tile;
        }
        logger.warn("Tile pack does not contain {}", tileId);
        return null;
    }

    public List<Tile> drawPrePlacedActiveTiles() {
        List<Tile> result = new ArrayList<>();
        for (Entry<String, TileGroup> entry: groups.entrySet()) {
            TileGroup group = entry.getValue();
            Iterator<Tile> i = group.tiles.iterator();
            while(i.hasNext()) {
                Tile tile = i.next();
                if (tile.getPosition() != null) {
                    if (group.state == TileGroupState.ACTIVE) {
                        result.add(tile);
                        i.remove();
                    } else {
                        tile.setPosition(null);
                        increaseSideMaskCounter(tile, entry.getKey());
                    }
                }
            }
        }
        return result;
    }

    public void addTile(Tile tile, String groupId) {
        TileGroup group = groups.get(groupId);
        if (group == null) {
            group = new TileGroup();
            groups.put(groupId, group);
        }
        group.tiles.add(tile);
        increaseSideMaskCounter(tile, groupId);
    }

    @Override
    public void setGroupState(String groupId, TileGroupState state) {
        //can be called with non-existing group (from expansion etc.)
        TileGroup group = groups.get(groupId);
        if (group != null) {
            group.state = state;
        }
    }

    @Override
    public TileGroupState getGroupState(String groupId) {
        TileGroup group = groups.get(groupId);
        if (group == null) return null;
        return group.state;
    }

    @Override
    public Set<String> getGroups() {
        return groups.keySet();
    }

    /* special Abbey related methods - TODO refactor it is here only for client */
    @Override
    public Tile getAbbeyTile() {
        for (Tile tile : groups.get(INACTIVE_GROUP).tiles) {
            if (tile.getId().equals(Tile.ABBEY_TILE_ID)) {
                return tile;
            }
        }
        return null;
    }

    @Override
    public int getSizeForEdgePattern(EdgePattern pattern) {
        int size = 0;
        for (EdgePattern filled : pattern.fill()) {
            Integer count = edgePatterns.get(filled);
            size += count == null ? 0 : count;
        }
        return size;
    }

}
