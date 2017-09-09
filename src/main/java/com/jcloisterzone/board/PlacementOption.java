package com.jcloisterzone.board;

import java.util.Objects;

import com.jcloisterzone.board.pointer.FeaturePointer;

/**
 * Represents allowed tile placement on particular board position.
 */
public class PlacementOption implements Comparable<PlacementOption> {

    private final Position position;
    private final Rotation rotation;

    /** not null if bridge must be places to get legal placement */
    private final FeaturePointer mandatoryBridge;

    /**
     * Instantiates a new {@code TilePlacement}.
     *
     * @param position        the position of the placement
     * @param rotation        the rotation of the placement
     * @param mandatoryBridge the position and location of the bridge, if mandatory
     */
    public PlacementOption(Position position, Rotation rotation, FeaturePointer mandatoryBridge) {
        this.position = position;
        this.rotation = rotation;
        this.mandatoryBridge = mandatoryBridge;
    }

    /**
     * Gets the position of this placement.
     *
     * @return the position  of this placement
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets rotation  of this placement.
     *
     * @return the rotation  of this placement
     */
    public Rotation getRotation() {
        return rotation;
    }

    /**
     * Gets the position and location of the eventual mandatory bridge.
     *
     * @return the position and location of the eventual mandatory bridge
     */
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
        PlacementOption other = (PlacementOption) obj;
        if (!Objects.equals(position, other.position)) return false;
        if (!Objects.equals(rotation, other.rotation)) return false;
        if (!Objects.equals(mandatoryBridge, other.mandatoryBridge)) return false;
        return true;
    }

    @Override
    public int compareTo(PlacementOption o) {
        int p = position.compareTo(o.position);
        if (p != 0) return p;
        return rotation.ordinal() -  o.rotation.ordinal();
    }
}
