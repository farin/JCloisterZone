package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;

public class FeatureEvent extends PlayEvent {
	
	final FeaturePointer fp;

	public FeatureEvent(int type, Player player, FeaturePointer fp) {
		super(type, player);
		this.fp = fp;
	}

	public FeatureEvent(Player player, FeaturePointer fp) {
		super(player);
		this.fp = fp;
	}
	
	public FeaturePointer getFeaturePointer() {
		return fp;
	}
	
	public Position getPosition() {
		return fp.getPosition();
	}
	
	public Location getLocation() {
		return fp.getLocation();
	}

}
