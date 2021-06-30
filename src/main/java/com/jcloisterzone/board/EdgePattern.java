package com.jcloisterzone.board;

import com.jcloisterzone.Immutable;
import io.vavr.collection.Map;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

@Immutable
public class EdgePattern implements Serializable {

    private static final long serialVersionUID = 1L;

    /** bit mask, concatenated edges N,E,S,W */
    int mask;

    /**
     * Instantiates a new {@code EdgePattern} with the given {@code mask}. The {@code mask} is intended to be constructed as a
     * sequence of 4 bits sequences defining the {@link EdgeType}, for the 4 directions West, South, East, and North,
     * left-to-right, in this order.
     *
     * @param mask the mask for the new instance
     */
    public EdgePattern(int mask) {
        this.mask = mask;
    }

    /**
     * Instantiates a new {@code EdgePattern} given the {@link EdgeType}s for the four sides.
     *
     * @param N the {@link EdgeType} for the north face
     * @param E the {@link EdgeType} for the east face
     * @param S the {@link EdgeType} for the south face
     * @param W the {@link EdgeType} for the west face
     */
    public EdgePattern(EdgeType N, EdgeType E, EdgeType S, EdgeType W) {
        this.mask = N.getMask() + (E.getMask() << 4) + (S.getMask() << 8) + + (W.getMask() << 12);
    }

    /**
     * Instantiates a new {@code EdgePattern} given the {@link EdgeType}s for the four sides encoded in a {@link Map}.
     *
     * @param edges the {@link EdgeType}s for the four sides encoded in a {@link Map}
     */
    public EdgePattern(Map<Location, EdgeType> edges) {
        this(
            edges.get(Location.N).get(),
            edges.get(Location.E).get(),
            edges.get(Location.S).get(),
            edges.get(Location.W).get()
        );
    }

    /**
     * Gets an instance constructed according to {@code str}. The format for {@code str} is a string exactly four
     * characters long, each of which is one of the {@link EdgeType}s: 'R' for road, 'C' for city, 'F' for field and
     * 'I' for river. In order, each character describe the type of the North, East, South, and West edges.
     *
     * @param str the string defining the {@link EdgePattern}
     * @return an instance constructed according to {@code str}
     */
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

    /**
     * Gets the {@link EdgeType} of the four edges in order North, East, South, and West.
     *
     * @return the {@link EdgeType} of the four edges
     */
    public EdgeType[] getEdges() {
        return new EdgeType[] {
            EdgeType.forMask(mask & 0xf),
            EdgeType.forMask((mask >> 4) & 0xf),
            EdgeType.forMask((mask >> 8) & 0xf),
            EdgeType.forMask((mask >> 12) & 0xf)
        };
    }

    /**
     * Gets the symmetry property for {@code this}.
     *
     * @return the symmetry property for {@code this}
     * @see TileSymmetry
     */
    public TileSymmetry getSymmetry() {
        EdgeType[] edges = getEdges();
        if (edges[0] == edges[1] && edges[0] == edges[2] && edges[0] == edges[3]) return TileSymmetry.S4;
        if (edges[0] == edges[2] && edges[1] == edges[3]) return TileSymmetry.S2;
        return TileSymmetry.NONE;
    }

    /**
     * Gets the {@link EdgeType} at location {@code loc}.
     *
     * @param loc the location to retrieve
     * @return the {@link EdgeType} at location {@code loc}
     */
    public EdgeType at(Location loc) {
        if (loc == Location.N) return EdgeType.forMask(mask & 15);
        if (loc == Location.E) return EdgeType.forMask((mask >> 4) & 15);
        if (loc == Location.S) return EdgeType.forMask((mask >> 8) & 15);
        if (loc == Location.W) return EdgeType.forMask((mask >> 12) & 15);
        throw new IllegalArgumentException();
    }

    /**
     * Gets a new instance matching {@code this} but rotated clockwise by {@code rot}.
     *
     * @param rot the rotation magnitude
     * @return a new instance matching {@code this} but rotated clockwise by {@code rot}
     */
    public EdgePattern rotate(Rotation rot) {
        if (rot == Rotation.R0) return this;
        java.util.List<EdgeType> l = Arrays.asList(getEdges());
        Collections.rotate(l, rot.ordinal());
        return new EdgePattern(l.get(0), l.get(1), l.get(2), l.get(3));
    }

    /**
     * Gets a new instance matching {@code this} but with the {@link EdgeType} at location {@code loc} replaced by
     * {@code type}.
     *
     * @param loc the location to replace
     * @param type the replacement {@link EdgeType}
     * @return a new instance matching {@code this} but with the {@link EdgeType} at location {@code loc} replaced by
     * {@code type}
     */
    public EdgePattern replace(Location loc, EdgeType type) {
        return new EdgePattern(
            loc == Location.N ? type : at(Location.N),
            loc == Location.E ? type : at(Location.E),
            loc == Location.S ? type : at(Location.S),
            loc == Location.W ? type : at(Location.W)
        );
    }

    @Deprecated //use rotate on EdgePattern instead //IntelliJ shows no usages of this... delete?
    public EdgeType at(Location loc, Rotation rotation) {
        return at(loc.rotateCCW(rotation));
    }

    /**
     * Counts the number of edges with unknown type.
     *
     * @return the number of edges with unknown type
     */
    public int wildcardSize() {
        return (int) Stream.of(getEdges())
            .filter(edge -> edge == EdgeType.UNKNOWN)
            .count();
    }

    /*
     * Having pattern for tile and empty position we need to know if they match regardless on rotations.
     * To avoid checking all tile rotation against all empty place pattern rotations this method return canonized form.
     * Canonized pattern is first one from ordering by Edge ordinals.
     */
    /**
     * Gets the canonized version of {@code this}. This is rotation-independent and can be used for matching.
     *
     * @return the canonized version of {@code this}
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

    /**
     * Checks whether {@code this} is an exact match of {@code ep}.
     *
     * @param ep the instance to match against
     * @return {@code true} if the instances match exactly, {@code false} otherwise
     */
    public boolean isMatchingExact(EdgePattern ep) {
        int m = mask & ep.mask;
        return ((m & 0xf) != 0) &&
                ((m & (0xf << 4)) != 0) &&
                ((m & (0xf << 8)) != 0) &&
                ((m & (0xf << 12)) != 0);

    }

    /**
     * Checks whether {@code this} matches some rotation of {@code ep}.
     *
     * @param ep the instance to match against
     * @return {@code true} if {@code this} matches some rotation of {@code ep}, {@code false} otherwise
     */
    public boolean isMatchingAnyRotation(EdgePattern ep) {
        for (Rotation rot : Rotation.values()) {
            if (rotate(rot).isMatchingExact(ep)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a bridge is allowed with this edge pattern.
     *
     * @param bridge the direction of the bridge to test
     * @return {@code true} if the bridge is allowed, {@code false} otherwise
     */
    public boolean isBridgeAllowed(Location bridge) {
        assert bridge == Location.NS || bridge == Location.WE;
        if (bridge == Location.NS) {
            if (at(Location.N) != EdgeType.FIELD) return false;
            if (at(Location.S) != EdgeType.FIELD) return false;
        } else {
            if (at(Location.W) != EdgeType.FIELD) return false;
            if (at(Location.E) != EdgeType.FIELD) return false;
        }
        return true;
    }

    /**
     * Gets the appropriate {@link EdgeType} to replace the one currently on the given {@code side}.
     *
     * @param side the side of interest
     * @return the appropriate {@link EdgeType} to replace the one currently on the given {@code side}
     */
    private EdgeType getBridgeReplacement(Location side) {
        switch (at(side)) {
        case FIELD: return EdgeType.ROAD;
        case UNKNOWN: return EdgeType.UNKNOWN;
        default: throw new IllegalArgumentException();
        }
    }

    /**
     * Gets a new instance that matches {@code this} but where a bridge is placed on the given location.
     *
     * @param bridge the location for the bridge
     * @return a new instance that matches {@code this} but where a bridge is placed on the given location
     */
    public EdgePattern getBridgePattern(Location bridge) {
        assert bridge == Location.NS || bridge == Location.WE;
        try {
            if (bridge == Location.NS) {
                return new EdgePattern(getBridgeReplacement(Location.N), at(Location.E), getBridgeReplacement(Location.S), at(Location.W));
            } else {
                return new EdgePattern(at(Location.N), getBridgeReplacement(Location.E), at(Location.S), getBridgeReplacement(Location.W));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pattern cannot be extended with " + bridge + "bridge.");
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