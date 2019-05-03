package com.jcloisterzone.board;

/**
 * Special case of city multi edge (hills and sheep weird tile)
 */
public class ShortEdge extends Edge {

    public ShortEdge(Position p1, Position p2) {
        super(p1, p2);
    }

    public ShortEdge(Position pos, Location loc) {
        super(pos, loc);
    }

    public ShortEdge(Edge edge) {
        super(edge.getP1(), edge.getP2());
    }

    @Override
    public ShortEdge translate(Position pos) {
        return new ShortEdge(super.translate(pos));
    }

    @Override
    public ShortEdge rotateCCW(Position origin, Rotation rot) {
        return new ShortEdge(super.rotateCCW(origin, rot));
    }

    @Override
    public ShortEdge rotateCW(Position origin, Rotation rot) {
        return new ShortEdge(super.rotateCW(origin, rot));
    }

    public Edge toEdge() {
        return new Edge(p1, p2);
    }

    @Override
    public String toString() {
        return String.format("ShortEdge(%s, %s)", p1, p2);
    }
}
