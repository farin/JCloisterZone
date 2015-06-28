package com.jcloisterzone.board.pointer;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;

/**
 * Points on feature on board or placed meeple.
 */
//do not extend Feature pointer to better type checking
public class MeeplePointer implements BoardPointer {

    private final FeaturePointer featurePointer;
    private final String meepleId;

    public MeeplePointer(FeaturePointer featurePointer, String meepleId) {
        this.featurePointer = featurePointer;
        this.meepleId = meepleId;
    }

    public MeeplePointer(Position position, Location location, String meepleId) {
        this(new FeaturePointer(position, location), meepleId);
    }

    public MeeplePointer(Meeple m) {
        this(m.getPosition(), m.getLocation(), m.getId());
        assert meepleId != null;
    }

    public FeaturePointer asFeaturePointer() {
        return featurePointer;
    }

    public Position getPosition() {
        return featurePointer.getPosition();
    }

    public Location getLocation() {
        return featurePointer.getLocation();
    }

    public String getMeepleId() {
        return meepleId;
    }

    public boolean match(Meeple meeple) {
        if (meeple == null) return false;
        return meepleId.equals(meeple.getId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[x=").append(getPosition().x).append(",y=")
                .append(getPosition().y).append(",").append(getLocation()).append(",")
                .append(meepleId).append("]").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((featurePointer == null) ? 0 : featurePointer.hashCode());
        result = prime * result + ((meepleId == null) ? 0 : meepleId.hashCode());
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
        MeeplePointer other = (MeeplePointer) obj;
        if (featurePointer == null) {
            if (other.featurePointer != null)
                return false;
        } else if (!featurePointer.equals(other.featurePointer))
            return false;
        if (meepleId == null) {
            if (other.meepleId != null)
                return false;
        } else if (!meepleId.equals(other.meepleId))
            return false;
        return true;
    }
}
