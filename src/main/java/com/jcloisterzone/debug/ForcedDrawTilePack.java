package com.jcloisterzone.debug;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileGroup;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.RandomGenerator;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Queue;

import java.util.Objects;

/**
 * Tile pack with predefined draw order.
 * Intended for debugging and integration test.
 * It can replace default tile pack using {@code annotations} property in saved
 * game. Mind that it works only for local games because annotations are not
 * propagated to remote clients.
 */
public class ForcedDrawTilePack extends TilePack {

    private final Queue<String> drawQueue;
    private final Integer drawLimit;


    public ForcedDrawTilePack(LinkedHashMap<String, TileGroup> groups, java.util.Map<String, Object> params) {
        this(groups, 0, paramsToDrawQueue(params), paramsToDrawLimit(params));
    }

    @SuppressWarnings("unchecked")
    private static Queue<String> paramsToDrawQueue(java.util.Map<String, Object> params) {
        if (params == null) {
            return Queue.empty();
        }
        java.util.List<String> drawOrder = (java.util.List<String>) params.get("drawOrder");
        return drawOrder == null ? Queue.empty() : Queue.ofAll(drawOrder);
    }

    private static Integer paramsToDrawLimit(java.util.Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Object value = params.get("drawLimit");
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return (Integer) value;
    }

    private ForcedDrawTilePack(LinkedHashMap<String, TileGroup> groups, int hiddenUnderHills, Queue<String> drawQueue, Integer drawLimit) {
        super(groups, hiddenUnderHills);
        this.drawQueue = drawQueue;
        this.drawLimit = drawLimit;
    }

    @Override
    public ForcedDrawTilePack setGroups(LinkedHashMap<String, TileGroup> groups) {
        if (getGroups() == groups) return this;
        return new ForcedDrawTilePack(groups, getHiddenUnderHills(), drawQueue, drawLimit);
    }

    @Override
    public ForcedDrawTilePack setHiddenUnderHills(int hiddenUnderHills) {
        if (getHiddenUnderHills() == hiddenUnderHills) return this;
        return new ForcedDrawTilePack(getGroups(), hiddenUnderHills, drawQueue, drawLimit);
    }

    private ForcedDrawTilePack setDrawList(Queue<String> drawQueue) {
        if (this.drawQueue == drawQueue) return this;
        return new ForcedDrawTilePack(getGroups(), getHiddenUnderHills(), drawQueue, drawLimit);
    }

    private ForcedDrawTilePack setDrawLimit(Integer drawLimit) {
        if (Objects.equals(this.drawLimit, drawLimit)) return this;
        return new ForcedDrawTilePack(getGroups(), getHiddenUnderHills(), drawQueue, drawLimit);
    }

    @Override
    public Tuple2<Tile, TilePack> drawTile(RandomGenerator random) {
        if (!drawQueue.isEmpty()) {
            Tuple2<String, Queue<String>> q = drawQueue.dequeue();
            Tuple2<Tile, TilePack> res = drawTile(q._1);
            return res.map2(_pack -> {
                ForcedDrawTilePack pack = (ForcedDrawTilePack) _pack;
                return pack.setDrawList(q._2);
            });
        }
        Tuple2<Tile, TilePack> res = super.drawTile(random);
        return decreaseTileLimit(res);
    }

    @Override
    public Tuple2<Tile, TilePack> drawTile(String groupName, String tileId) {
        Tuple2<Tile, TilePack> res = super.drawTile(groupName, tileId);
        return decreaseTileLimit(res);
    }

    private Tuple2<Tile, TilePack> decreaseTileLimit(Tuple2<Tile, TilePack> res) {
        if (drawLimit == null) {
            return res;
        }
        return res.map2(_pack -> {
            ForcedDrawTilePack pack = (ForcedDrawTilePack) _pack;
            return pack.setDrawLimit(pack.drawLimit - 1);
        });
    }

    @Override
    public int totalSize() {
        if (drawLimit != null) {
            return drawLimit;
        }
        return super.totalSize();
    }


    @Override
    public int size() {
        if (drawLimit != null) {
            return drawLimit;
        }
        // if "." is at the end, quit game on that element
        if (!drawQueue.isEmpty() && drawQueue.last().equals("#END")) {
            return drawQueue.size() - 1;
        }
        return super.size();
    }

    @Override
    protected int getInternalSize() {
        //don't affect internal size by drawLimit
        return super.size() + getHiddenUnderHills();
    }

    public Queue<String> getDrawQueue() {
        return drawQueue;
    }

}
