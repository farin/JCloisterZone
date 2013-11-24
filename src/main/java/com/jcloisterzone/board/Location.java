package com.jcloisterzone.board;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * bite order                     corresponding constants
 *
 *     0  1                               1  2
 *   7      2                          128     4
 *   6      3                          64      8
 *     5  4                              32  16
 */
public class Location implements Serializable {

    private static final long serialVersionUID = -8348910171518350353L;

    transient private String name;
    private int mask;

    private static Map<Integer, Location> instances = new HashMap<>();

    /**
     * Obtains instance with given mask. For named location
     * instance is unique.
     * @param mask	bite mask of demand instance
     */
    public static Location create(int mask) {
        Location loc = instances.get(mask);
        if (loc != null) return loc;
        //TODO prepare name here
        return new Location(null, mask);
    }

    private Object readResolve() throws ObjectStreamException {
        return create(mask);
    }

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
    public static final Location CLOISTER = new Location("CLOISTER", 1 << 16 );
    /** Tower on tile */
    public static final Location TOWER = new Location("TOWER", 1 << 17);
    /** Inprisoned follwer */
    public static final Location PRISON = new Location("PRISON", 1 << 18);

    // --- farm locations ---

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
    /** Center farm*/
    public static final Location INNER_FARM = new Location("INNER_FARM", 0);


    private static final Location[] SIDES = {N, E, S, W};
    private static final Location[] DIAGONAL_SIDES = {NE, SE, SW, NW};
    private static final Location[] FARM_SIDES = {NL, NR, EL, ER, SL, SR, WL, WR};


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Location)) return false;
        return mask == ((Location)obj).mask;
    }

    @Override
    public int hashCode() {
        return mask;
    }

    /** Rotation about quarter circle clockwise */
    public Location next() {
        return shift(2);
    }

    /** Rotation about quarter circle counter-clockwise */
    public Location prev() {
        return shift(6);
    }

    /** Returns opposite location, mirrored by axis */
    public Location rev() {
        // odd bits shift by 5, even by 3;
        int mLo = mask & 255;
        mLo = ((mLo & 85) << 5) | ((mLo & 170) << 3);
        mLo = (mLo | (mLo >> 8)) & 255;

        int mHi =  (mask & 65280) >> 8;
        mHi = ((mHi & 85) << 5) | ((mHi & 170) << 3);
        mHi = (mHi | (mHi >> 8)) & 255;

        return create((mask & ~65535) | (mHi << 8) | mLo);
    }

    /**
     * Bitwise mask rotation about given number of bites.
     * @param i 	number of bites to rotate
     * @return rotated location
     */
    private Location shift(int i) {
        int mLo = (mask & 255) << i;
        mLo = (mLo | mLo >> 8) & 255;

        int mHi = (mask & 65280) << i;
        mHi = (mHi | mHi >> 8) & 65280;

        return create((mask & ~65535) | mHi | mLo);
    }

    /**
     * Relative rotations in counter-clockwise location
     * @param d how much rotate
     * @return rotated location
     */
    public Location rotateCCW(Rotation rot) {
        return shift((rot.ordinal()*6)%8);
    }

    /**
     * Relative rotations in clockwise location
     */
    public Location rotateCW(Rotation rot) {
        return shift(rot.ordinal()*2);
    }

    public static Location[] sides() {
        return SIDES;
    }

    public static Location[] sidesFarm() {
        return FARM_SIDES;
    }

    public static Location[] sidesDiagonal() {
        return DIAGONAL_SIDES;
    }

    public Location getLeftFarm() {
        assert isEdgeLocation();
        return create((mask >> 8) & 85);
    }

    public Location getRightFarm() {
        assert isEdgeLocation();
        return create((mask >> 8) & 170);
    }


    /** Checks if this is part of given location */
    public boolean isPartOf(Location d) {
        if (mask == 0) return this == d;
        return ((mask ^ d.mask) & mask) == 0;
    }

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

    /** Merge two locations together */
    public Location union(Location d) {
        if (d == null) return this;
        assert !isSpecialLocation() && !(isEdgeLocation() ^ d.isEdgeLocation()) & !(isFarmLocation() ^ d.isFarmLocation()) : "union("+this+','+d+')';
        return create(mask | d.mask);
    }

    /** Subtract given location from this */
    public Location substract(Location d) {
        if (d == null) return this;
        assert !isSpecialLocation() && !(isEdgeLocation() ^ d.isEdgeLocation()) & !(isFarmLocation() ^ d.isFarmLocation()) : "substract("+this+','+d+')';
        return create((~(mask & d.mask)) & mask);
    }

    public Location intersect(Location d) {
        if (d == null || (mask & d.mask) == 0) return null;
        assert !isSpecialLocation() && !(isEdgeLocation() ^ d.isEdgeLocation()) & !(isFarmLocation() ^ d.isFarmLocation()) : "interasect("+this+','+d+')';
        return create(mask & d.mask);
    }

    public Location[] intersectMulti(Location[] locs) {
        List<Location> result = new ArrayList<>();
        for (Location loc: locs) {
            Location i = this.intersect(loc);
            if (i != null) {
                result.add(i);
            }
        }
        return result.toArray(new Location[result.size()]);
    }

    /** Creates instance according to name */
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

    public Rotation getRotationOf(Location loc) {
        for (Rotation r : Rotation.values()) {
            if (this.equals(loc.rotateCW(r))) return r;
        }
        return null;
    }

    /**
     * Check if this rotated another location
     */
    public boolean isRotationOf(Location loc) {
        return getRotationOf(loc) != null;
    }

    //assertion methods

    public boolean isFarmLocation() {
        return this == INNER_FARM || (mask & 255) > 0;
    }

    public boolean isEdgeLocation() {
        return (mask & 65280) > 0;
    }

    public boolean isSpecialLocation() {
        return (mask & ~65535) > 0;
    }
}
