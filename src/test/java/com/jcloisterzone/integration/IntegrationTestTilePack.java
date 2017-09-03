package com.jcloisterzone.integration;

import java.util.ArrayList;

import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroup;
import com.jcloisterzone.board.TilePack;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Queue;

public class IntegrationTestTilePack extends TilePack {

    private final Queue<String> drawList;

    public IntegrationTestTilePack(LinkedHashMap<String, TileGroup> groups, ArrayList<String> draw) {
        this(groups, Queue.ofAll(draw));
    }

    private IntegrationTestTilePack(LinkedHashMap<String, TileGroup> groups, Queue<String> drawList) {
        super(groups);
        this.drawList = drawList;
    }

    public IntegrationTestTilePack setGroups(LinkedHashMap<String, TileGroup> groups) {
        if (getGroups() == groups) return this;
        return new IntegrationTestTilePack(groups, drawList);
    }

    private IntegrationTestTilePack setDrawList(Queue<String> drawList) {
        if (this.drawList == drawList) return this;
        return new IntegrationTestTilePack(getGroups(), drawList);
    }

    @Override
    public Tuple2<TileDefinition, TilePack> drawTile(int index) {
        if (!drawList.isEmpty()) {
            Tuple2<String, Queue<String>> q = drawList.dequeue();
            Tuple2<TileDefinition, TilePack> res = drawTile(q._1);
            return res.map2(pack -> ((IntegrationTestTilePack)pack).setDrawList(q._2));
        }
        return super.drawTile(index);
    }

    @Override
    public int size() {
        // if "." is at the end, quit game on that element
        if (!drawList.isEmpty() && drawList.last().equals(".")) {
            return drawList.size() - 1;
        }
        return super.size();
    }

}
