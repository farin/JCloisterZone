package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

public abstract class Event {

    private final Player player;
    private final Position position;
    private final Location location;

    public Event() {
        this(null, null, null);
    }

    public Event(Player player) {
        this(player, null, null);
    }

    public Event(Position position) {
        this(null, position);
    }

    public Event(Player player, Position position) {
        this(player, position, null);
    }

    public Event(Player player, Position position, Location location) {
        this.player = player;
        this.position = position;
        this.location = location;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getPosition() {
        return position;
    }

    public Location getLocation() {
        return location;
    }

}
