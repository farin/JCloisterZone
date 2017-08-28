package com.jcloisterzone.board;


import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.Immutable;

/** Edge between to positions */
@Immutable
public class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    final Position p1, p2;

    public Edge(Position p1, Position p2) {
        assert !p1.equals(p2);
        if (p1.compareTo(p2) > 0) {
            this.p1 = p2;
            this.p2 = p1;
        } else {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    public Edge(Position pos, Location loc) {
        this(pos, pos.add(loc));
    }

    public Edge translate(Position pos) {
        return new Edge(p1.add(pos), p2.add(pos));
    }

    public Edge rotateCW(Position origin, Rotation rot) {
        return new Edge(
            p1.rotateCW(origin, rot),
            p2.rotateCW(origin, rot)
        );
    }

    public Edge rotateCCW(Position origin, Rotation rot) {
        return new Edge(
            p1.rotateCCW(origin, rot),
            p2.rotateCCW(origin, rot)
        );
    }

    public boolean isHorizontal() {
        return p1.x == p2.x;
    }

    public boolean isVertical() {
        return p1.y == p2.y;
    }

    public Position getP1() {
        return p1;
    }

    public Position getP2() {
        return p2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof Edge)) return false;
        Edge e = (Edge) obj;
        return Objects.equals(p1, e.p1) && Objects.equals(p2, e.p2);
    }

    @Override
    public String toString() {
        return String.format("Edge(%s, %s)", p1, p2);
    }
}
