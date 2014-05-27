package com.jcloisterzone.board.pointer;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

public class FeaturePointer {

    private final Position position;
    private final Location location;

    public FeaturePointer(Position position, Location location) {
        this.position = position;
        this.location = location;
    }

    public Position getPosition() {
        return position;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[x=").append(position.x).append(",y=")
                .append(position.y).append(",").append(location).append("]").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result
                + ((position == null) ? 0 : position.hashCode());
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
        FeaturePointer other = (FeaturePointer) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        return true;
    }



}
