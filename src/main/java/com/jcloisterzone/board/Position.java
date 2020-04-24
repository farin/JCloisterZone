package com.jcloisterzone.board;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;


/**
 * Represents position on board. Immutable class.
 *
 * @author Roman Krejcik
 */
@Immutable
public class Position implements BoardPointer, Comparable<Position> {

    private static final long serialVersionUID = 1L;

    public final int x;
    public final int y;

    public static Position ZERO = new Position(0, 0);

    public static final LinkedHashMap<Location, Position> ADJACENT = LinkedHashMap.of(
        Location.N, new Position(0, -1),
        Location.E, new Position(1, 0),
        Location.S, new Position(0, 1),
        Location.W, new Position(-1, 0)
    );

    public static final Map<Location, Position> ADJACENT_AND_DIAGONAL = ADJACENT.merge(HashMap.of(
        Location.NE, new Position(1, -1),
        Location.SE, new Position(1, 1),
        Location.SW, new Position(-1, 1),
        Location.NW, new Position(-1, -1)
    ));


    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position p) {
        this(p.x, p.y);
    }

    @Override
    public Position getPosition() {
        return this;
    }

    @Override
    public FeaturePointer asFeaturePointer() {
        return new FeaturePointer(this, null);
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", x, y);
    }

    public Position add(Position p) {
        return new Position(x + p.x, y + p.y);
    }

    public Position subtract(Position p) {
        return new Position(x - p.x, y - p.y);
    }

    public Position rotateCW(Rotation rot) {
        switch (rot) {
        case R0: return this;
        case R90: return new Position(-y, +x);
        case R180: return new Position(-x, -y);
        case R270: return new Position(+y, -x);
        }
        throw new IllegalArgumentException();
    }

    public Position rotateCCW(Rotation rot) {
        return rotateCW(rot.inverse());
    }

    public Position rotateCW(Position origin, Rotation rot) {
        return subtract(origin).rotateCW(rot).add(origin);
    }

    public Position rotateCCW(Position origin, Rotation rot) {
        return subtract(origin).rotateCCW(rot).add(origin);
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

    @Override
    public int compareTo(Position o) {
        if (y == o.y) {
            return x - o.x;
        }
        return y - o.y;
    }
}
