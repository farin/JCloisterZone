package com.jcloisterzone.board.pointer;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;

/**
 * Points on feature on board or placed meeple.
 */
public class MeeplePointer extends FeaturePointer {

    private final String meepleId;

//    private final Class<? extends Meeple> meepleType;
//    private final Player meepleOwner;

    public MeeplePointer(Position position, Location location, String meepleId) {
        super(position, location);
        this.meepleId = meepleId;
    }

    public MeeplePointer(Meeple m) {
        this(m.getPosition(), m.getLocation(), m.getId());
        assert meepleId != null;
    }

    public String getMeepleId() {
        return meepleId;
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
        int result = super.hashCode();
        result = prime * result + ((meepleId == null) ? 0 : meepleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        MeeplePointer other = (MeeplePointer) obj;
        if (meepleId == null) {
            if (other.meepleId != null)
                return false;
        } else if (!meepleId.equals(other.meepleId))
            return false;
        return true;
    }


//    public Class<? extends Meeple> getMeepleType() {
//        return meepleType;
//    }
//
//    public Player getMeepleOwner() {
//        return meepleOwner;
//    }



}
