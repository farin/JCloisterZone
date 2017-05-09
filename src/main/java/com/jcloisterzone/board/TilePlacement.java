package com.jcloisterzone.board;

import java.util.Objects;

import com.jcloisterzone.board.pointer.FeaturePointer;

/**
 * Represents allowed tile placement on particular board position.
 */
public class TilePlacement implements Comparable<TilePlacement> {

    private final Position position;
    private final Rotation rotation;

    /** not null if bridge must be places to get legal placement */
    private final FeaturePointer mandatoryBridge;

    public TilePlacement(Position position, Rotation rotation, FeaturePointer mandatoryBridge) {
        this.position = position;
        this.rotation = rotation;
        this.mandatoryBridge = mandatoryBridge;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public FeaturePointer getMandatoryBridge() {
        return mandatoryBridge;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(position).append(",").append(rotation);
        if (mandatoryBridge != null) {
            sb.append(",bridge=");
            sb.append(mandatoryBridge);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, rotation, mandatoryBridge);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TilePlacement other = (TilePlacement) obj;
        if (!Objects.equals(position, other.position)) return false;
        if (!Objects.equals(rotation, other.rotation)) return false;
        if (!Objects.equals(mandatoryBridge, other.mandatoryBridge)) return false;
        return true;
    }

    @Override
    public int compareTo(TilePlacement o) {
        int p = position.compareTo(o.position);
        if (p != 0) return p;
        return rotation.ordinal() -  o.rotation.ordinal();
    }
}
