package com.jcloisterzone.debug;

import java.util.ArrayList;

import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroup;
import com.jcloisterzone.board.TilePack;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Queue;

/**
 * Tile pack with predefined draw order.
 * Intended for debugging and integration test.
 * It can replace default tile pack using {@code annotations} property in saved
 * game. Mind that it works only for local games because annotations are not
 * propagated to remote clients.
 */
public class ForcedDrawTilePack extends TilePack {

    private final Queue<String> drawList;

    public ForcedDrawTilePack(LinkedHashMap<String, TileGroup> groups, ArrayList<String> draw) {
        this(groups, Queue.ofAll(draw));
    }

    private ForcedDrawTilePack(LinkedHashMap<String, TileGroup> groups, Queue<String> drawList) {
        super(groups);
        this.drawList = drawList;
    }

    public ForcedDrawTilePack setGroups(LinkedHashMap<String, TileGroup> groups) {
        if (getGroups() == groups) return this;
        return new ForcedDrawTilePack(groups, drawList);
    }

    private ForcedDrawTilePack setDrawList(Queue<String> drawList) {
        if (this.drawList == drawList) return this;
        return new ForcedDrawTilePack(getGroups(), drawList);
    }

    @Override
    public Tuple2<TileDefinition, TilePack> drawTile(int index) {
        if (!drawList.isEmpty()) {
            Tuple2<String, Queue<String>> q = drawList.dequeue();
            Tuple2<TileDefinition, TilePack> res = drawTile(q._1);
            return res.map2(pack -> ((ForcedDrawTilePack)pack).setDrawList(q._2));
        }
        return super.drawTile(index);
    }

    @Override
    public int size() {
        // if "." is at the end, quit game on that element
        if (!drawList.isEmpty() && drawList.last().equals("#END")) {
            return drawList.size() - 1;
        }
        return super.size();
    }

}
