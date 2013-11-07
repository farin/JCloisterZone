package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTilePack implements TilePack {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INACTIVE_GROUP = "inactive";

    private Map<String, ArrayList<Tile>> groups = new HashMap<>();
    private Set<String> activeGroups = new HashSet<>();

    private Map<EdgePattern, Integer> edgePatterns = new HashMap<>();

    @Override
    public int totalSize() {
        int n = 0;
        for (Entry<String, ArrayList<Tile>> entry: groups.entrySet()) {
            if (!entry.getKey().equals(INACTIVE_GROUP)) {
                n += entry.getValue().size();
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
        for (String key: activeGroups) {
            n += groups.get(key).size();
        }
        return n;
    }

    @Override
    public Tile drawTile(int index) {
        for (String key: activeGroups) {
            ArrayList<Tile> group = groups.get(key);
            if (index < group.size()) {
                Tile currentTile = group.remove(index);
                decreaseSideMaskCounter(currentTile, key);
                return currentTile;
            } else {
                index -= group.size();
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
        ArrayList<Tile> group = groups.get(groupId);
        Iterator<Tile> i = group.iterator();
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
        for (Entry<String, ArrayList<Tile>> entry: groups.entrySet()) {
            ArrayList<Tile> group = entry.getValue();
            Iterator<Tile> i = group.iterator();
            while(i.hasNext()) {
                Tile tile = i.next();
                if (tile.getPosition() != null) {
                    if (activeGroups.contains(entry.getKey())) {
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
        if (!groups.containsKey(groupId)) {
            groups.put(groupId, new ArrayList<Tile>());
        }
        ArrayList<Tile> group = groups.get(groupId);
        group.add(tile);
        increaseSideMaskCounter(tile, groupId);
    }

    @Override
    public void activateGroup(String group) {
        //check if group exists - game load can cause activation on empty (and non existing after load) group
        if (groups.containsKey(group)) {
            activeGroups.add(group);
        }
    }

    @Override
    public void deactivateGroup(String group) {
        activeGroups.remove(group);
    }

    public void deactivateAllGroups() {
        activeGroups.clear();
    }

    @Override
    public boolean isGroupActive(String group) {
        return activeGroups.contains(group);
    }

    @Override
    public Set<String> getGroups() {
        return groups.keySet();
    }

    /* special Abbey related methods - refactor je to jen kvuli klientovi */
    @Override
    public Tile getAbbeyTile() {
        for (Tile tile : groups.get(INACTIVE_GROUP)) {
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
