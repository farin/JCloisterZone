package com.jcloisterzone.board;


import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.Immutable;

/**
 * Represents an edge between two positions.
 */
@Immutable
public class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    final Position p1, p2;

    /**
     * Constructs a new instance given two positions.
     *
     * @param p1 one position
     * @param p2 the other position
     */
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

    /**
     * Constructs a new instance given a position and a location pointing in a direction.
     *
     * @param pos a position
     * @param loc a location pointing in a direction
     */
    public Edge(Position pos, Location loc) {
        this(pos, pos.add(loc));
    }

    /**
     * Returns a new instance that is translated from {@code this} by {@code pos}.
     *
     * @param pos a position indicating a translation
     * @return a new instance that is translated from {@code this} by {@code pos}
     */
    public Edge translate(Position pos) {
        return new Edge(p1.add(pos), p2.add(pos));
    }

    /**
     * Returns a new instance that is rotated clockwise by {@code rot} around {@code origin}.
     *
     * @param origin the center for the rotation
     * @param rot the rotation magnitude
     * @return a new instance that is rotated clockwise by {@code rot} around {@code origin}
     */
    public Edge rotateCW(Position origin, Rotation rot) {
        return new Edge(
            p1.rotateCW(origin, rot),
            p2.rotateCW(origin, rot)
        );
    }

    /**
     * Returns a new instance that is rotated counter-clockwise by {@code rot} around {@code origin}.
     *
     * @param origin the center for the rotation
     * @param rot the rotation magnitude
     * @return a new instance that is rotated counter-clockwise by {@code rot} around {@code origin}
     */
    public Edge rotateCCW(Position origin, Rotation rot) {
        return new Edge(
            p1.rotateCCW(origin, rot),
            p2.rotateCCW(origin, rot)
        );
    }

    /**
     * Checks whether this edge is horizontal or not. Notice that this is perpendicular to the relative position of the
     * two tiles that make the edge.
     *
     * @return {@code true} if the edge is horizontal, {@code false} otherwise
     */
    public boolean isHorizontal() {
        return p1.x == p2.x;
    }

    /**
     * Checks whether this edge is vertical or not. Notice that this is perpendicular to the relative position of the
     * two tiles that make the edge.
     *
     * @return {@code true} if the edge is vertical, {@code false} otherwise
     */
    public boolean isVertical() {
        return p1.y == p2.y;
    }

    /**
     * Returns the right/bottom position among the two forming this edge.
     * @return the right/bottom position among the two forming this edge
     */
    public Position getP1() {
        return p1;
    }

    /**
     * Returns the left/top position among the two forming this edge.
     * @return the left/top position among the two forming this edge
     */
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
