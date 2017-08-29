package com.jcloisterzone.board;

import java.io.ObjectStreamException;
import java.io.Serializable;

import com.jcloisterzone.Immutable;

import io.vavr.collection.List;
import io.vavr.collection.Vector;


/**
 * Represents locations on a tile. A location is any "space" where tile features, such as rivers, roads, farms, abbots,
 * etc. can be located.
 * Examples of locations include:
 * - a south-to-east feature-space (e.g., a river, a road, a city)
 * - a south-to-north feature-space (e.g., a river, a road, a city)
 * - a city space spanning in all directions
 * - an abbot space
 * - a cloister space
 * - a flier space
 * - a tower space
 * - a farm-space on the left of the west side
 * - a farm space facing no sides (surrounded by other features)
 *
 * Multiple locations in the same tile can coexist and they are represented using bits as flags.
 *
 *   Bit order                 Constants for farm locations            Constants for roads/rivers/cities
 *
 *
 *     0  1                               1  2                                   256 512
 *   7      2                          128     4                             32768     1024
 *   6      3                          64      8                             16384     2048
 *     5  4                              32  16                                 8192 4096
 *
 *  City/road/river locations are shifted by 8 bit so they can coexist with farm locations.
 */
@Immutable
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    transient private String name;
    private int mask;

    private static java.util.Map<Integer, Location> instances = new java.util.HashMap<>();

    /**
     * Returns an instance with the given {@code mask}. Multiple calls with the same mask will return the same instance.
     * This includes named instances (e.g. {@link #SW}.
     * @param mask bit mask of the instance required
     * @return an instance with the given {@code mask}
     */
    public static Location create(int mask) {
        if (mask == 0) return null;
        Location loc = instances.get(mask);
        if (loc != null) return loc;
        //TODO prepare name here
        return new Location(null, mask);
    }

    private Object readResolve() throws ObjectStreamException {
        return create(mask);
    }

    public int getMask() {
        return mask;
    }

    /**
     * Constructs an instance with the given {@code name} and {@code mask}.
     * @param name the name for the instance
     * @param mask the mask for the instance
     */
    private Location(String name, int mask) {
        this.name = name;
        this.mask = mask;

        instances.put(mask, this);
    }

    /** North */
    public static final Location N = new Location("N", 3 << 8);
    /** West */
    public static final Location W = new Location("W", 192 << 8);
    /** South */
    public static final Location S = new Location("S", 48 << 8);
    /** East */
    public static final Location E = new Location("E", 12 << 8);

    /** North-west */
    public static final Location NW = new Location("NW", 195 << 8);
    /** South-west */
    public static final Location SW = new Location("SW", 240 << 8);
    /** South-east */
    public static final Location SE = new Location("SE", 60 << 8);
    /** North-east */
    public static final Location NE = new Location("NE", 15 << 8);

    /** Horizontal location - W + E */
    public static final Location WE = new Location("WE", 204 << 8);
    /** Vertical location -  N + S */
    public static final Location NS = new Location("NS", 51 << 8);
    /** All edge locations */
    public static final Location NWSE = new Location("NWSE", 255 << 8);

    /** Supplement to the north */
    public static final Location _N = new Location("_N", 252 << 8);
    /** Supplement to the west  */
    public static final Location _W = new Location("_W", 63 << 8);
    /** Supplement to the south */
    public static final Location _S = new Location("_S", 207 << 8);
    /** Supplement to the east */
    public static final Location _E = new Location("_E", 243 << 8);

    /** Cloister on tile */
    public static final Location CLOISTER = new Location("CLOISTER", 1 << 18);
    /** on monastery as Abbot */
    public static final Location ABBOT = new Location("ABBOT", 1 << 19);
    /** Tower on tile */
    public static final Location TOWER = new Location("TOWER", 1 << 20);
    /** Flier location - follower can be placed here just for moment, before dice roll  */
    public static final Location FLIER = new Location("FLIER", 1 << 21);
    /** City of Carcassonne specials (Count) */
    public static final Location QUARTER_CASTLE = new Location("QUARTER_CASTLE", 1 << 22);
    public static final Location QUARTER_MARKET = new Location("QUARTER_MARKET", 1 << 23);
    public static final Location QUARTER_BLACKSMITH = new Location("QUARTER_BLACKSMITH", 1 << 24);
    public static final Location QUARTER_CATHEDRAL = new Location("QUARTER_CATHEDRAL", 1 << 25);

    // --- farm locations ---

    /** Inner farm*/
    public static final Location INNER_FARM = new Location("INNER_FARM", 1 << 16);
    /** for tiles with two inner farms */
    public static final Location INNER_FARM_B = new Location("INNER_FARM_B", 1 << 17);

    /** North left farm */
    public static final Location NL = new Location("NL", 1);
    /** North right farm */
    public static final Location NR = new Location("NR", 2);
    /** East left farm */
    public static final Location EL = new Location("EL", 4);
    /** East right farm */
    public static final Location ER = new Location("ER", 8);
    /** South left farm */
    public static final Location SL = new Location("SL", 16);
    /** South right farm */
    public static final Location SR = new Location("SR", 32);
    /** West left farm */
    public static final Location WL = new Location("WL", 64);
    /** West right farm */
    public static final Location WR = new Location("WR", 128);

    public static final List<Location> SIDES = List.of(N, E, S, W);
    public static final List<Location> FARM_SIDES = List.of(NL, NR, EL, ER, SL, SR, WL, WR);
    public static final List<Location> BRIDGES = List.of(NS, WE);
    public static final List<Location> QUARTERS = List.of(QUARTER_CASTLE, QUARTER_MARKET, QUARTER_BLACKSMITH, QUARTER_CATHEDRAL);

    /**
     * Returns {@code true} if {@code this} instance and {@code obj} have the same mask, {@code false} otherwise.
     * @param obj the instance to compare
     * @return {@code true} if {@code this} instance and {@code obj} have the same mask, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Location)) return false;
        return mask == ((Location)obj).mask;
    }

    /**
     * Returns the mask of {@code this} instance.
     * @return the mask of {@code this} instance
     */
    @Override
    public int hashCode() {
        return mask;
    }

    /**
     * Returns an instance with the same mask as {@code this} but rotated by 90 degrees clockwise.
     * @return the rotated instance
     */
    public Location next() {
        return shift(2);
    }

    /**
     * Returns an instance with the same mask as {@code this} but rotated by 90 degrees counter-clockwise.
     * @return the rotated instance
     */
    public Location prev() {
        return shift(6);
    }

    /**
     * Returns an instance with the same mask as {@code this} but mirrored.
     * @return the mirrored instance
     */
    public Location rev() {
        // odd bits shift by 5, even by 3;
        int mLo = mask & 0xff;
        mLo = ((mLo & 0b1010101) << 5) | ((mLo & 0b10101010) << 3);
        mLo = (mLo | (mLo >> 8)) & 0xff;

        int mHi =  (mask & 0xff00) >> 8;
        mHi = ((mHi & 0b1010101) << 5) | ((mHi & 0b10101010) << 3);
        mHi = (mHi | (mHi >> 8)) & 0xff;

        return create((mask & ~65535) | (mHi << 8) | mLo);
    }

    /**
     * Clockwise bitwise mask rotation.
     * @param i number of bits to rotate
     * @return rotated instance
     */
    private Location shift(int i) {
        int mLo = (mask & 0xff) << i;
        mLo = (mLo | mLo >> 8) & 0xff;

        int mHi = (mask & 0xff00) << i;
        mHi = (mHi | mHi >> 8) & 0xff00;

        return create((mask & ~0xffff) | mHi | mLo);
    }

    /**
     * Returns an instance with the same mask as {@code this} but rotated by {@code rot} counter-clockwise.
     * @param rot how much rotation to apply
     * @return the rotated instance
     */
    public Location rotateCCW(Rotation rot) {
        return shift((rot.ordinal()*6)%8);
    }

    /**
     * Returns an instance with the same mask as {@code this} but rotated by {@code rot} clockwise.
     * @param rot how much rotation to apply
     * @return the rotated instance
     */
    public Location rotateCW(Rotation rot) {
        return shift(rot.ordinal()*2);
    }

    public Location getLeftFarm() {
        assert isEdgeLocation();
        return create((mask >> 8) & 0b1010101);
    }

    public Location getRightFarm() {
        assert isEdgeLocation();
        return create((mask >> 8) & 0b10101010);
    }


    /**
     * Checks if {@code this} is part of {@code loc}.
     *
     * @param loc the location to compare
     * @return {@code true} if {@code this} is part of {@code loc}, {@code false} otherwise
     */
    public boolean isPartOf(Location loc) {
        if (mask == 0) return this == loc;
        return ((mask ^ loc.mask) & mask) == 0;
    }

    /**
     * Converts {@code this} to a string. The string can be parsed back by using the {@link #valueOf} method.
     * @return the string representing {@code this}
     */
    @Override
    public String toString() {
        if (name != null) return name;
        StringBuilder str = new StringBuilder();
        for (Location atom : FARM_SIDES) {
            if (intersect(atom) != null) {
                if (str.length() > 0) str.append(".");
                str.append(atom);
            }
        }
        return str.toString();
    }

    /**
     * Merges two locations together by applying a bitwise OR to their masks.
     * @param loc the location to merge to {@code this}
     * @return the location resulting from the merge
     */
    public Location union(Location loc) {
        if (loc == null) return this;
        assert !isSpecialLocation() && ((isEdgeLocation() == loc.isEdgeLocation()) & isFarmLocation() == loc.isFarmLocation()) : "union("+this+','+loc+')';
        return create(mask | loc.mask);
    }

    /**
     * Subtracts two locations and returns a new one having as mask only the bits in the mask of {@code this} that are
     * not in the mask of {@code loc}
     * @param loc the location to subtract from {@code this}
     * @return the location resulting from the subtraction
     */
    public Location substract(Location loc) {
        if (loc == null) return this;
        assert !isSpecialLocation() && isEdgeLocation() == loc.isEdgeLocation() & isFarmLocation() == loc.isFarmLocation() : "substract("+this+','+loc+')';
        return create((~(mask & loc.mask)) & mask);
    }

    /**
     * Intersects two locations by applying a bitwise AND to their masks.
     * @param loc the location to intersect with {@code this}
     * @return the location resulting from the intersection
     */
    public Location intersect(Location loc) {
        if (loc == null || (mask & loc.mask) == 0) return null;
        assert !isSpecialLocation() && isEdgeLocation() == loc.isEdgeLocation() & isFarmLocation() == loc.isFarmLocation() : "intersect("+this+','+loc+')';
        return create(mask & loc.mask);
    }

    /**
     * Splits {@code this} in its sides components.
     * @return the sides components of {@code this}
     */
    public List<Location> splitToSides() {
        return Location.SIDES.filter(side -> intersect(side) != null);
    }

    /**
     * Creates instance according to {@code name}. This is done by merging all instances corresponding to the names
     * specified in {@code name} and separated by a '.' character. For example, "N.ABBOT" will return a location
     * indicating a road/river/city space facing north and an abbot space.
     *
     * @param name the named instances to merge separated by a '.'
     * @return the merged named locations
     */
    public static Location valueOf(String name) {
        Location value = null;
        for (String part : name.split("\\.")) {
            try {
                Location item = (Location) Location.class.getField(part).get(null);
                value = item.union(value);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unknown location " + name, ex);
            }
        }
        assert value != null;
        return value;
    }

    /**
     * Compares every possible rotation of #{@code loc} with {@code this} instance. If one of them matches, that
     * rotation is returned; otherwise, null is returned.
     * @param loc the location to rotate and compare
     * @return the rotation making {@code loc} match {@code this} or {@code null} if no such rotation exists
     */
    public Rotation getRotationOf(Location loc) {
        for (Rotation r : Rotation.values()) {
            if (this.equals(loc.rotateCW(r))) return r;
        }
        return null;
    }

    /**
     * Checks if {@code this} is a rotation of {@code loc}.
     * @param loc the location to compare
     * @return {@code true} if {@code this} is a rotation of {@code loc}, {@code false} otherwise
     */
    public boolean isRotationOf(Location loc) {
        return getRotationOf(loc) != null;
    }

    /* get included full farm coners */
    public Vector<Corner> getCorners() {
        if (!isFarmLocation()) {
            return Vector.empty();
        }
        Vector<Corner> res = Vector.empty();
        if (WR.isPartOf(this) && NL.isPartOf(this)) res = res.append(Corner.NW);
        if (NR.isPartOf(this) && EL.isPartOf(this)) res = res.append(Corner.NE);
        if (ER.isPartOf(this) && SL.isPartOf(this)) res = res.append(Corner.SE);
        if (SR.isPartOf(this) && WL.isPartOf(this)) res = res.append(Corner.SW);
        return res;
    }


    /**
     * Checks if {@code this} is a farm location.
     * @return {@code true} if {@code this} is a farm location, {@code false} otherwise
     */
    public boolean isFarmLocation() {
        return ((mask & 0x30000) | (mask & 0xFF)) > 0;
    }

    /**
     * Checks if {@code this} is an edge location.
     * @return {@code true} if {@code this} is an edge location, {@code false} otherwise
     */
    public boolean isEdgeLocation() {
        return (mask & 0xFF00) > 0;
    }

    /**
     * Checks if {@code this} is a special location.
     * @return {@code true} if {@code this} is a special location, {@code false} otherwise
     */
    public boolean isSpecialLocation() {
        return (mask & ~0x3FFFF) > 0;
    }

    /**
     * Checks if {@code this} is a bridge location.
     * @return {@code true} if {@code this} is a bridge location, {@code false} otherwise
     */
    public boolean isBridgeLocation() {
        return BRIDGES.contains(this);
    }

    /**
     * Checks if {@code this} is a city quarter location (i.e., bridge, castle, market or cathedral).
     * @return {@code true} if {@code this} is a city quarter location, {@code false} otherwise
     */
    public boolean isCityOfCarcassonneQuarter() {
        return QUARTERS.contains(this);
    }
}
