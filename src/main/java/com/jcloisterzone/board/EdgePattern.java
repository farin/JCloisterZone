package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import com.jcloisterzone.Immutable;

import io.vavr.collection.Map;

@Immutable
public class EdgePattern implements Serializable {

    private static final long serialVersionUID = 1L;

    /** bit mask, concatenated edges W,S,E,N */
    int mask;

    public EdgePattern(int mask) {
        this.mask = mask;
    }

    public EdgePattern(EdgeType N, EdgeType E, EdgeType S, EdgeType W) {
        this.mask = N.getMask() + (E.getMask() << 4) + (S.getMask() << 8) + + (W.getMask() << 12);
    }

    public EdgePattern(Map<Location, EdgeType> edges) {
        this(
            edges.get(Location.N).get(),
            edges.get(Location.E).get(),
            edges.get(Location.S).get(),
            edges.get(Location.W).get()
        );
    }

    public static EdgePattern fromString(String str) {
        if (str.length() != 4) {
            throw new IllegalArgumentException();
        }
        return new EdgePattern(
            EdgeType.forChar(str.charAt(0)),
            EdgeType.forChar(str.charAt(1)),
            EdgeType.forChar(str.charAt(2)),
            EdgeType.forChar(str.charAt(3))
        );
    }

    public EdgeType[] getEdges() {
        return new EdgeType[] {
            EdgeType.forMask(mask & 15),
            EdgeType.forMask((mask >> 4) & 15),
            EdgeType.forMask((mask >> 8) & 15),
            EdgeType.forMask((mask >> 12) & 15)
        };
    }

    public TileSymmetry getSymmetry() {
        EdgeType[] edges = getEdges();
        if (edges[0] == edges[1] && edges[0] == edges[2] && edges[0] == edges[3]) return TileSymmetry.S4;
        if (edges[0] == edges[2] && edges[1] == edges[3]) return TileSymmetry.S2;
        return TileSymmetry.NONE;
    }

    public EdgeType at(Location loc) {
        if (loc == Location.N) return EdgeType.forMask(mask & 15);
        if (loc == Location.E) return EdgeType.forMask((mask >> 4) & 15);
        if (loc == Location.S) return EdgeType.forMask((mask >> 8) & 15);
        if (loc == Location.W) return EdgeType.forMask((mask >> 12) & 15);
        throw new IllegalArgumentException();
    }

    public EdgePattern rotate(Rotation rot) {
        if (rot == Rotation.R0) return this;
        java.util.List<EdgeType> l = Arrays.asList(getEdges());
        Collections.rotate(l, rot.ordinal());
        return new EdgePattern(l.get(0), l.get(1), l.get(2), l.get(3));
    }

     public EdgePattern replace(Location loc, EdgeType type) {
        return new EdgePattern(
            loc == Location.N ? type : at(Location.N),
            loc == Location.E ? type : at(Location.E),
            loc == Location.S ? type : at(Location.S),
            loc == Location.W ? type : at(Location.W)
        );
     }

    @Deprecated //use rotate on EdgePattern instead
    public EdgeType at(Location loc, Rotation rotation) {
        return at(loc.rotateCCW(rotation));
    }

    public int wildcardSize() {
        return (int) Stream.of(getEdges())
            .filter(edge -> edge == EdgeType.UNKNOWN)
            .count();
    }

    /**
     * Having pattern for tile and empty position we need to know if they match regardless on rotations.
     * To avoid checking all tile rotation against all empty place pattern rotations this method return canonized form.
     * Canonized pattern is first one from ordering by Edge ordinals.
     */
    public EdgePattern canonize() {
        EdgePattern min = this;
        for (Rotation rot : Rotation.values()) {
            EdgePattern ep = rotate(rot);
            if (ep.mask < min.mask) {
                min = ep;
            }
        }
        return min;
    }

    public boolean isMatchingExact(EdgePattern ep) {
        int m = mask & ep.mask;
        return ((m & 15) != 0) &&
                ((m & (15 << 4)) != 0) &&
                ((m & (15 << 8)) != 0) &&
                ((m & (15 << 12)) != 0);

    }

    public boolean isMatchingAnyRotation(EdgePattern ep) {
        for (Rotation rot : Rotation.values()) {
            if (rotate(rot).isMatchingExact(ep)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBridgeAllowed(Location bridge) {
        assert bridge == Location.NS || bridge == Location.WE;
        if (bridge == Location.NS) {
            if (at(Location.N) != EdgeType.FARM) return false;
            if (at(Location.S) != EdgeType.FARM) return false;
        } else {
            if (at(Location.W) != EdgeType.FARM) return false;
            if (at(Location.E) != EdgeType.FARM) return false;
        }
        return true;
    }

    private EdgeType getBridgeReplacement(Location side) {
        switch (at(side)) {
        case FARM: return EdgeType.ROAD;
        case UNKNOWN: return EdgeType.UNKNOWN;
        default: throw new IllegalArgumentException();
        }
    }

    public EdgePattern getBridgePattern(Location bridge) {
        assert bridge == Location.NS || bridge == Location.WE;
        try {
            if (bridge == Location.NS) {
                return new EdgePattern(getBridgeReplacement(Location.N), at(Location.E), getBridgeReplacement(Location.S), at(Location.W));
            } else {
                return new EdgePattern(at(Location.N), getBridgeReplacement(Location.E), at(Location.S), getBridgeReplacement(Location.W));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pattern annot be extended with " + bridge + "bridge.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EdgePattern)) return false;
        EdgePattern that = (EdgePattern) obj;
        return that.canonize().mask == canonize().mask;
    }

    @Override
    public int hashCode() {
        return mask;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s%s",
            at(Location.N).asChar(),
            at(Location.E).asChar(),
            at(Location.S).asChar(),
            at(Location.W).asChar()
        );
    }
}
