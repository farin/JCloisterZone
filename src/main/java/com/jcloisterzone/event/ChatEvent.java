package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class ChatEvent extends Event {

	private final Player player;
    private final String message;

    public ChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    public Player getPlayer() {
		return player;
	}
}
