package com.jcloisterzone.board;

import java.util.Set;

public interface TilePack {

    String INACTIVE_GROUP = "inactive";

    int totalSize();
    boolean isEmpty();
    int size();

    Tile drawTile(int index);
    Tile drawTile(String groupId, String tileId);
    Tile drawTile(String tileId);

    /* special Abbey related methods - refactor je to jen kvuli klientovi */
    Tile getAbbeyTile();

    void setGroupState(String groupId, TileGroupState state);
    TileGroupState getGroupState(String groupId);
    Set<String> getGroups();

    int getSizeForEdgePattern(EdgePattern edgePattern);
}
