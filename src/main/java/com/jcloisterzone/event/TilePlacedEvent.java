package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;

public class TilePlacedEvent extends Event {

    private final Tile tile;

    public TilePlacedEvent(Player player, Tile tile) {
        super(player, tile.getPosition());
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

}
