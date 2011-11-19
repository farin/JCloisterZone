package com.jcloisterzone.board;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;


/**
 * bite order                     corresponding constants
 *
 *     0  1                               1  2
 *   7      2                          128     4
 *   6      3                          64      8
 *     5  4                              32  16
 */
public class Location implements Serializable {

	private static final long serialVersionUID = -8348910171518350352L;

	private String name;
	private int mask;

	private static Map<Integer, Location> instances = Maps.newHashMap();

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
	public static final Location N = new Location("N", 3);
	/** West */
	public static final Location W = new Location("W", 192);
	/** South */
	public static final Location S = new Location("S", 48);
	/** East */
	public static final Location E = new Location("E", 12);

	/** North-west */
	public static final Location NW = new Location("NW", 195);
	/** South-west */
	public static final Location SW = new Location("SW", 240);
	/** South-east */
	public static final Location SE = new Location("SE", 60);
	/** North-east */
	public static final Location NE = new Location("NE", 15);

	/** Cloister on tile */
	public static final Location CLOISTER = new Location("CLOISTER", 256);
	/** Tower on tile */
	public static final Location TOWER = new Location("TOWER", 512);

	/** Horizontal location - W + E */
	public static final Location HORIZONTAL = new Location("HORIZONTAL", 204);
	/** Vertical location -  N + S */
	public static final Location VERTICAL = new Location("VERTICAL", 51);
	/** All locations, whole tile - N + S + W + E  */
	public static final Location ALL = new Location("ALL", 255);


	/** Supplement to the north */
	public static final Location _N = new Location("_N", 252);
	/** Supplement to the west  */
	public static final Location _W = new Location("_W", 63);
	/** Supplement to the south */
	public static final Location _S = new Location("_S", 207);
	/** Supplement to the east */
	public static final Location _E = new Location("_E", 243);

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
	public static final Location CENTER = new Location("CENTER", 0);


	private static final Location[] SIDES = {N, E, S, W};
	private static final Location[] DIAGONAL_SIDES = {NE, SE, SW, NW};
	private static final Location[] FARM_SIDES = {NL, NR, EL, ER, SL, SR, WL, WR};


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (! (obj instanceof Location)) return false;
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
		if (! isSideLocation()) return this; //non-side location
		// liche bity posunout o 5, sude o 3;
		int m = ((mask & 85) << 5) | ((mask & 170) << 3);
		return create((m | (m >> 8)) & 255);
	}

	/**
	 * Bitwise mask rotation about given number of bites.
	 * @param i 	number of bites to rotate
	 * @return rotated location
	 */
	private Location shift(int i) {
		if (! isSideLocation()) return this; //non-side location
		int m = mask << i;
		return create((m | (m >> 8)) & 255);
	}

	/**
	 * Relative rotations in counter-clockwise location
	 * @param d how much rotate
	 * @return rotated location
	 */
	//TODO no loop
	public Location rotateCCW(Rotation rot) {
		Location ret = this;
		for (int i = 0; i < rot.ordinal(); i++)
			ret = ret.prev();
		return ret;
	}

	/**
	 * Relative rotations in clockwise location
	 */
	public Location rotateCW(Rotation rot) {
		Location ret = this;
		for (int i = 0; i < rot.ordinal(); i++)
			ret = ret.next();
		return ret;
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


	@Deprecated
	public Location farmHalfSide(int i) {
		int m = (i==0) ? mask & 85: mask & 170;
		return create(m);
	}

	private boolean isSideLocation() {
		return (mask & 255) != 0;
	}

	/** Checks if this is part of given location */
	public boolean isPartOf(Location d) {
		if (! isSideLocation()) return d == this;
		if (((mask ^ d.mask) & mask) == 0) return true;
		return false;
	}


	public int  getMask() {
		return mask;
	}


	@Override
	public String toString() {
		if (name != null)  return name;
		StringBuilder str = new StringBuilder();
		for(Location atom : FARM_SIDES) {
			if (intersect(atom) != null) {
				if (str.length() > 0) str.append("+");
				str.append(atom);
			}
		}
		return str.toString();
	}

	/** Merge two locations together */
	public Location union(Location d) {
		if (d == null) return this;
		if (! d.isSideLocation() || ! isSideLocation()) throw new IllegalArgumentException("Not side locations: " + this + "," + d );
		return create(mask | d.mask);
	}

	/** Subtract given location from this */
	public Location substract(Location d) {
		if (d == null) return this;
		if (! d.isSideLocation() || ! isSideLocation()) throw new IllegalArgumentException("Not side location: " + this + "," + d);
		return create((~(mask & d.mask)) & mask);
	}

	public Location intersect(Location d) {
		if (d == null || (mask & d.mask) == 0) return null;
		return create(mask & d.mask);
	}

	/** Creates instance according to name */
	public static Location valueOf(String name) {
		Location value = null;
		for(String part : name.split("\\+")) {
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

	public static Location valueOfIndex(int index) {
		if (index == 8) return CENTER;
		return create(1 << index);
	}

	public Rotation getRotationOf(Location loc) {
		for(Rotation r : Rotation.values()) {
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

	//debug code

	public boolean isFarmLocation() {
		return this == CENTER || Arrays.asList(FARM_SIDES).contains(this);
	}


}
