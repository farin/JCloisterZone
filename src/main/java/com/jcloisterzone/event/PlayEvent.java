package com.jcloisterzone.event;

import com.jcloisterzone.Player;

/**
 * Ancestor for all in-game event.
 */
public abstract class PlayEvent extends Event {
	
	//TODO there should be always triggering player (but it is true now, eg meeple events)
	private final Player player;
	
	public PlayEvent() {
		this(null);
	}
	
	public PlayEvent(Player player) {
		super();
		this.player = player;
	}

	public PlayEvent(int type, Player player) {
		super(type);
		this.player = player;
	}

    public Player getPlayer() {
        return player;
    }

}
