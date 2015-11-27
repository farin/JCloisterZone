package com.jcloisterzone.board;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;

public class EdgePattern {

    private Edge[] edges = new Edge[4];

    private EdgePattern() { }
    private EdgePattern(Edge[] edges) { this.edges = edges; }

    private static Edge getTileEdgePattern(Tile tile, Location loc) {
        if (tile.getRiver() != null && loc.isPartOf(tile.getRiver())) {
            return Edge.RIVER;
        }

        Feature f = tile.getFeaturePartOf(loc);
        if (f == null) {
            return Edge.FARM;
        }
        if (f instanceof Road) {
            return Edge.ROAD;
        }
        return Edge.CITY;
    }

    public static EdgePattern forTile(Tile tile) {
        EdgePattern pattern = new EdgePattern();
        for (Location loc : Location.sides()) {
            pattern.edges[indexfor(loc)] = getTileEdgePattern(tile, loc);
        }
        return pattern;
    }

    private static int indexfor (Location loc) {
        if (loc == Location.N) return 0;
        if (loc == Location.W) return 1;
        if (loc == Location.S) return 2;
        if (loc == Location.E) return 3;
        throw new IllegalArgumentException();
    }

    public static EdgePattern forEmptyTile(Board board, Position pos) {
        EdgePattern pattern = new EdgePattern();
        for (Location loc : Location.sides()) {
            Tile t = board.get(pos.add(loc));
            int idx = indexfor(loc);
            if (t == null) {
                pattern.edges[idx] = Edge.UNKNOWN;
            } else {
                pattern.edges[idx] = getTileEdgePattern(t, loc.rev());
            }
        }
        return pattern;
    }

    public Edge at(Location loc) {
        return edges[indexfor(loc)];
    }

    public Edge at(Location loc, Rotation rotation) {
        return at(loc.rotateCCW(rotation));
    }

    public int wildcardSize() {
        int size = 0;
        for (int i = 0; i < edges.length; i++) {
            if (edges[i] == Edge.UNKNOWN) size++;
        }
        return size;
    }

    private EdgePattern switchEdge(int i, Edge edge) {
        Edge[] switched = Arrays.copyOf(edges, edges.length);
        switched[i] = edge;
        return new EdgePattern(switched);
    }

    /**
     * For EdgePatterns with wildcards generates all valid combinations (without wildcards)
     * eg: RRC? -> RRCV, RRCR, RRCC, RRCF
     */
    public Collection<EdgePattern> fill() {
        //TODO better impl
        if (wildcardSize() == 0) return Collections.singleton(this);
        Queue<EdgePattern> q = new LinkedList<EdgePattern>();
        q.add(this);
        while(q.peek().wildcardSize() > 0) {
            EdgePattern p = q.poll();
            int i = 0;
            while(p.edges[i] != Edge.UNKNOWN) i++;
            q.add(switchEdge(i, Edge.RIVER));
            q.add(switchEdge(i, Edge.ROAD));
            q.add(switchEdge(i, Edge.CITY));
            q.add(switchEdge(i, Edge.FARM));
        }
        return q;
    }

    private Edge[] shift(int shift) {
        Edge[] result = new Edge[4];
        for (int i = 0; i < edges.length; i++) {
            result[i] = edges[(i+shift)%edges.length];
        }
        return result;
    }

    /**
     * Having pattern for tile and empty position we need to know if they match regardless on rotations.
     * To avoid checking all tile rotation against all empty place pattern rotations this method return canonized form.
     * Canonized pattern is first one from ordering by Edge ordinals.
     */
    private Edge[] canonize() {
        Edge[] result = edges;
        shiftLoop:
        for (int shift = 1; shift < edges.length; shift++) {
            Edge[] e = shift(shift);
            for (int i = 0; i < edges.length; i++) {
                if (e[i].ordinal() < result[i].ordinal()) {
                    result = e;
                    continue shiftLoop;
                }
                if (e[i].ordinal() > result[i].ordinal()) {
                    break;
                }
            }
        }
        return result;
    }

    public boolean isBridgeAllowed(Location bridge, Rotation tileRotation) {
        if (bridge == Location.NS) {
            if (at(Location.N, tileRotation) != Edge.FARM) return false;
            if (at(Location.S, tileRotation) != Edge.FARM) return false;
        } else {
            if (at(Location.W, tileRotation) != Edge.FARM) return false;
            if (at(Location.E, tileRotation) != Edge.FARM) return false;
        }
        return true;
    }

    public EdgePattern getBridgePattern(Location bridge) {
        Edge[] bridgeCode = Arrays.copyOf(edges, edges.length);
        if (bridge == Location.NS) {
            bridgeCode[0] = Edge.ROAD;
            bridgeCode[2] = Edge.ROAD;
        } else {
            bridgeCode[1] = Edge.ROAD;
            bridgeCode[3] = Edge.ROAD;
        }
        return new EdgePattern(bridgeCode);
    }

    public EdgePattern removeBridgePattern(Location bridge) {
        Edge[] bridgeCode = Arrays.copyOf(edges, edges.length);
        if (bridge == Location.NS) {
            bridgeCode[0] = Edge.FARM;
            bridgeCode[2] = Edge.FARM;
        } else {
            bridgeCode[1] = Edge.FARM;
            bridgeCode[3] = Edge.FARM;
        }
        return new EdgePattern(bridgeCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EdgePattern)) return false;
        EdgePattern that = (EdgePattern) obj;
        return Arrays.equals(that.canonize(), canonize());
    }

    @Override
    public int hashCode() {
        Edge[] edges = canonize();
        int hash = 0;
        for (int i = 0; i < edges.length; i++) {
            hash = hash * 91 + edges[i].ordinal();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < edges.length; i++) {
            sb.append(edges[i]);
        }
        return sb.toString();
    }
}
