package com.jcloisterzone.board;

import java.io.Serializable;



/**
 * Reperesents position on board. Immutable class.
 *
 * @author farin
 */
public class Position implements Serializable {

	public final int x;
	public final int y;

	private static final long serialVersionUID = -345L;

	/**
	 * Initializes a new instance.
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Position(Position p) {
		this(p.x,p.y);
	}

	public String toString() {
		return "[x=" + x + ",y=" + y + "]";
	}

	public Position add(Location loc) {
		int x = this.x;
		int y = this.y;
		if (Location.N.isPartOf(loc)) y--;
		if (Location.S.isPartOf(loc)) y++;
		if (Location.W.isPartOf(loc)) x--;
		if (Location.E.isPartOf(loc)) x++;
		return new Position(x,y);
	}

	public Location diff(Position p) {
		if (x == p.x) {
			if (y == p.y + 1) return Location.N;
			if (y == p.y - 1) return Location.S;
			return null;
		}
		if (x == p.x - 1) {
			if (y == p.y) return Location.E;
			if (y == p.y + 1) return Location.NE;
			if (y == p.y - 1) return Location.SE;
			return null;
		}
		if (x == p.x + 1) {
			if (y == p.y) return Location.W;
			if (y == p.y + 1) return Location.NW;
			if (y == p.y - 1) return Location.SW;
			return null;
		}
		return null;
	}

	public int squareDistance(Position p) {
		//orthogonal distance
		return Math.abs(x - p.x) + Math.abs(y - p.y);
	}

	@Override
	public int hashCode() {
		return (x << 16) ^ y;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
		    Position p = (Position)obj;
		    return (x == p.x) && (y == p.y);
		}
		return false;
	}


}