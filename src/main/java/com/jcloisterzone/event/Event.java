package com.jcloisterzone.event;

import com.jcloisterzone.Player;

/**
 * Ancestor for all events including non-game events like setup and chat.
 */
public abstract class Event {

    private final int type;
    private final Player player;

    public Event() {
        this(0, null);
    }

    public Event(Player player) {
        this(0, player);
    }

    public Event(int type) {
        this(type, null);
    }

    public Event(int type, Player player) {
        this.type = type;
        this.player = player;
    }

    public int getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

}
