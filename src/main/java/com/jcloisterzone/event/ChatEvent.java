package com.jcloisterzone.event;

import com.jcloisterzone.Player;

public class ChatEvent extends Event {

    private final String message;

    public ChatEvent(Player player, String message) {
        super(player);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
