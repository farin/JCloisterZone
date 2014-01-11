package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;

public abstract class PlayEvent extends Event {

    private final Position position;
    private final Location location;

    public PlayEvent() {
        this(0, null, null, null);
    }

    public PlayEvent(Player player) {
        this(0, player, null, null);
    }

    public PlayEvent(Player player, Position position) {
        this(0, player, position, null);
    }

    public PlayEvent(Player player, Position position, Location location) {
        this(0, player, position, location);
    }

    public PlayEvent(int type, Player player) {
        this(type, player, null, null);
    }

    public PlayEvent(int type, Player player, Position position) {
        this(type, player, position, null);
    }

    public PlayEvent(int type, Player player, Position position, Location location) {
        super(type, player);
        this.position = position;
        this.location = location;
    }

    public Position getPosition() {
        return position;
    }

    public Location getLocation() {
        return location;
    }

}
