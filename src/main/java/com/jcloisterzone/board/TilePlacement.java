package com.jcloisterzone.board;


/**
 * Represents allowed tile placement on particular board position.
 */
public class TilePlacement implements Comparable<TilePlacement> {

    private final Position position;
    private final Rotation rotation;

    public TilePlacement(Position position, Rotation rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "[x=" + position.x + ",y=" + position.y + "," + rotation + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((position == null) ? 0 : position.hashCode());
        result = prime * result
                + ((rotation == null) ? 0 : rotation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TilePlacement other = (TilePlacement) obj;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (rotation != other.rotation)
            return false;
        return true;
    }

    @Override
    public int compareTo(TilePlacement o) {
        int p = position.compareTo(o.position);
        if (p != 0) return p;
        return rotation.ordinal() -  o.rotation.ordinal();
    }
}
