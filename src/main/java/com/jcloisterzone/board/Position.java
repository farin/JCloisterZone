package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;


/**
 * Represents position on board. Immutable class.
 *
 * @author Roman Krejcik
 */
public class Position implements Serializable {

	public final int x;
	public final int y;

	private static final long serialVersionUID = -345L;

	public static final Map<Location, Position> ADJACENT;
	public static final Map<Location, Position> ADJACENT_AND_DIAGONAL;

	static {
		ADJACENT = new ImmutableMap.Builder<Location, Position>()
		 .put(Location.N, new Position(0, -1))
		 .put(Location.E, new Position(1, 0))
		 .put(Location.S, new Position(0, 1))
		 .put(Location.W, new Position(-1, 0))
		 .build();

		ADJACENT_AND_DIAGONAL= new ImmutableMap.Builder<Location, Position>()
		 .putAll(ADJACENT)
		 .put(Location.NE, new Position(1, -1))
		 .put(Location.SE, new Position(1, 1))
		 .put(Location.SW, new Position(-1, 1))
		 .put(Location.NW, new Position(-1, -1))
		 .build();
	}

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

	public Position add(Position p) {
		return new Position(x+p.x, y+p.y);
	}

	public Position[] addMulti(Position[] offsets) {
		Position[] result = new Position[offsets.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = add(offsets[i]);
		}
		return result;
	}

	public Position[] addMulti(Collection<Position> offsets) {
		Position[] arr = new Position[offsets.size()];
		arr = offsets.toArray(arr);
		return addMulti(arr);
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