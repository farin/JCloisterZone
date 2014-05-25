package com.jcloisterzone.board.pointer;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;

/**
 * Points on feature on board or placed meeple.
 * Meeples of samee type are undistinguishable.  
 */
public class MeeplePointer {
	
	private final Position position;
	private final Location location;
	
	private final Class<? extends Meeple> meepleType; 
	private final Player meepleOwner;
	

	
	public MeeplePointer(Position position, Location location, Class<? extends Meeple> meepleType, Player meepleOwner) {
		this.position = position;
		this.location = location;
		this.meepleType = meepleType;
		this.meepleOwner = meepleOwner;
	}
	
	public MeeplePointer(Meeple m) {
		this(m.getPosition(), m.getLocation(), m.getClass(), m.getPlayer());
		assert m.getPosition() != null;
	}

	public Position getPosition() {
		return position;
	}

	public Location getLocation() {
		return location;
	}

	public Class<? extends Meeple> getMeepleType() {
		return meepleType;
	}

	public Player getMeepleOwner() {
		return meepleOwner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result
				+ ((meepleOwner == null) ? 0 : meepleOwner.hashCode());
		result = prime * result
				+ ((meepleType == null) ? 0 : meepleType.getSimpleName().hashCode());
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
		MeeplePointer other = (MeeplePointer) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (meepleOwner == null) {
			if (other.meepleOwner != null)
				return false;
		} else if (!meepleOwner.equals(other.meepleOwner))
			return false;
		if (meepleType == null) {
			if (other.meepleType != null)
				return false;
		} else if (!meepleType.equals(other.meepleType))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}
	
	
}
