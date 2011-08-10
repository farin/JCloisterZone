package com.jcloisterzone.board;

import java.io.ObjectStreamException;
import java.io.Serializable;


/**
 * <p>
 * Type safe enumeration. Instances are used for several purposes.<br>
 * <ul>
 * <li>Pure location - like north, south ... - used for relative positions
 * 	of tiles on a board
 * <li>Score object (<code>Road</code>, <code>City</code>, <code>Cloister</code>
 *  or <code>Farm</code>) area on a tile - can be simple location same as in
 * first case but additionally can represent combined locations like north-west or
 * supplements to the simple ones.
 * In this case combined needn't be continuous (for example north+south).
 * For farms finer identification must be used, so each side is divided
 * into two parts, left and right. For example, two farms separated by road
 * into left and right part can lay near the north edge.
 * Left and right is relative to a tile center, so on south left is on east side.
 * Combined locations can consist from left and right parts too, not
 * only from a whole sides.<br>
 * Special locations <code>CENTER</code> and <code>ALL</code> are used for
 * positioning on a tile to.
 * </ul>
 * <p>
 * Important and basic location are named.<br>
 * <code>N</code>,<code>E</code>,<code>S</code>,<code>W</code> - simple locations<br>
 * <code>NL</code>,<code>NR</code>,<code>EL</code><code>ER</code>... - halves of simple locations<br>
 * <code>NW</code>,<code>NE</code>,<code>SW</code>,<code>SE</code> - diagonal locations<br>
 * <code>_N</code>,<code>_E</code>,<code>_S</code>,<code>_W</code> - suplements to simple ones<br>
 * <code>HORIZONTAL</code>,<code>VERTICAL</code> - union of opposite simple locations<br>
 * <code>ALL</code> - means all location - used for city place over whole tile
 * <code>CENTER</code> - means none direcion or in center of tile - cloister location or
 * 	farm not conncted to any edge<br>
 * <br>
 * All location are obtained by <code>factory<code> which ensure that named
 * location are unique. They can be compared by <code>==</code> operator.
 * <p>
 * <code>Direction</code> is stored as bite mask of 8 bites. This
 * representation correspondes to second purpose (score object placement)
 * and each bite shows presence half of simple location area.<br>
 * 0. NL, 1. NR, 2. EL ...<br>
 * It can be graphically illustrated:<br>
 * <pre>
 * bite order                     corresponding constants
 *
 *     0  1                               1  2
 *   7      2                          128     4
 *   6      3                          64      8
 *     5  4                              32  16
 * </pre>
 * Combined locations are bitwise OR of masks. CENTER location has mask equals to zero.
 *
 * @author farin
 */
public class Location implements Serializable {

	private static final long serialVersionUID = -8348910171518350352L;

	protected Integer ordinal;
	protected String name;
	protected int mask;

	/**
	 * Obtains instance with given mask. For named location
	 * instance is unique.
	 * @param mask	bite mask of demand instance
	 */
	public static Location create(int mask) {
		switch (mask) {
			case 3: return N;
			case 12: return E;
			case 48: return S;
			case 192: return W;
			case 195: return NW;
			case 15: return NE;
			case 240: return SW;
			case 60: return SE;
			case 0: return CENTER;
			case 204: return HORIZONTAL;
			case 51: return VERTICAL;
			case 252: return _N;
			case 243: return _E;
			case 207: return _S;
			case 63: return _W;
			case 255: return ALL;
			case 1: return NL;
			case 2: return NR;
			case 4: return EL;
			case 8: return ER;
			case 16: return SL;
			case 32: return SR;
			case 64: return WL;
			case 128: return WR;
			case 256: return CLOISTER;
			case 512: return TOWER;
		}
		return new Location(null, null, mask);
	}

	private Object readResolve() throws ObjectStreamException {
		return create(mask);
	}

	private Location(Integer ordinal, String name, int mask) {
		this.name = name;
		this.mask = mask;
		this.ordinal = ordinal;
	}

	/** North */
	public static final Location N = new Location(0, "N", 3);
	/** West */
	public static final Location W = new Location(3, "W", 192);
	/** South */
	public static final Location S = new Location(2, "S", 48);
	/** East */
	public static final Location E = new Location(1, "E", 12);

	/** North-west */
	public static final Location NW = new Location(0, "NW", 195);
	/** South-west */
	public static final Location SW = new Location(3, "SW", 240);
	/** South-east */
	public static final Location SE = new Location(2, "SE", 60);
	/** North-east */
	public static final Location NE = new Location(1, "NE", 15);

	/** Non-side locations */
	public static final Location CLOISTER = new Location(0, "CLOISTER", 256);
	public static final Location TOWER = new Location(0, "TOWER", 512);

	/** Horizontal location - means W + E + CENTER */
	public static final Location HORIZONTAL = new Location(null, "HORIZONTAL", 204);
	/** Vertical location - means N + S + CENTER */
	public static final Location VERTICAL = new Location(null, "VERTICAL", 51);
	/** All locations, whole tile - N+ S + W + E + CENTER */
	public static final Location ALL = new Location(null, "ALL", 255);

	/** Supplement to the north */
	public static final Location _N = new Location(0, "_N", 252);
	/** Supplement to the west  */
	public static final Location _W = new Location(3, "_W", 63);
	/** Supplement to the south */
	public static final Location _S = new Location(2, "_S", 207);
	/** Supplement to the east */
	public static final Location _E = new Location(1, "_E", 243);

	/* farm base areas */
	/** North left */
	public static final Location NL = new Location(0, "NL", 1);
	/** North right */
	public static final Location NR = new Location(1, "NR", 2);
	/** East left */
	public static final Location EL = new Location(2, "EL", 4);
	/** East right */
	public static final Location ER = new Location(3, "ER", 8);
	/** South left */
	public static final Location SL = new Location(4, "SL", 16);
	/** South right */
	public static final Location SR = new Location(5, "SR", 32);
	/** West left */
	public static final Location WL = new Location(6, "WL", 64);
	/** West right */
	public static final Location WR = new Location(7, "WR", 128);

	public static final Location CENTER = new Location(8, "CENTER", 0);

	private static final Location[] SIDES = {N, E, S, W};
	private static final Location[] DIAGONAL_SIDES = {NE, SE, SW, NW};
	private static final Location[] FARM_SIDES = {NL, NR, EL, ER, SL, SR, WL, WR};


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (! (obj instanceof Location)) return false;
		if (mask == ((Location)obj).mask) return true;
		return false;
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
	 * Returns location index. Direction index is 'natural' if have sense or throws exception
	 * For example simple locations (N, E, S, W) have indexes 0,1,2,3.
	 * Farm simple locations have indexes 0..7
	 */
	public int ordinal() {
		if (ordinal == null) {
			throw new IllegalStateException("Ordinal is not defined for location " + name);
		}
		return ordinal;
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

	/**
	 * Returns left or right part of simple location. Have sence only
	 * for <code>N</code>,<code>E</code>,<code>S</code> or <code>W</code>.
	 */
	public Location farmHalfSide(int i) {
		int m = (i==0) ? mask & 85: mask & 170;
		return create(m);
	}

	/**
	 * Check if location is simple. Only for roads and city.
	 */
	public boolean isSimple() {
		if (this == N || this == S || this == E ||
			this == W || this == CENTER) return true;
		return false;
	}

	/**
	 * Returns one of simple location contained in this.
	 * @param e	element for which need simple element
	 * @return simple element for given element type
	 */
	public Location getSimple() {
		if (! isSideLocation()) return this;
		for(int i = 0; i < 8; i++) {
			//highest bit
			if ((mask & (1 << i)) > 0) return create(1 << i);
		}
		return this; //should never reach this
	}

	public boolean isSideLocation() {
		return (mask & 255) != 0;
	}

//	@Deprecated
//	public List<Location> getSideSimples() {
//		if (! isSideLocation()) return Collections.singletonList(this); //non-side location
//		List<Location> simples = Lists.newArrayList();
//		if (ft == FeatureType.FARM) {
//			for(int i = 0; i < 8; i++) {
//				if ((mask & (1 << i)) > 0) simples.add(create(1 << i));
//			}
//		} else {
//			for(Location d : SIDES) {
//				if (d.isPartOf(this)) simples.add(d);
//			}
//		}
//		return simples;
//	}

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

	/**
	 * Check if this rotated another location
	 */
	public boolean isRotationOf(Location d) {
		for(int i=0; i < 4; i++) {
			if (mask == d.mask) return true;
			d = d.next();
		}
		return false;
	}


}
