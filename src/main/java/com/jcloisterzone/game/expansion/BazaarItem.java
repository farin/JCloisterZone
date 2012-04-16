package com.jcloisterzone.game.expansion;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;

public class BazaarItem {

    private final Tile tile;
    private Player owner;
    private boolean drawn;

    public BazaarItem(Tile tile) {
        this.tile = tile;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Tile getTile() {
        return tile;
    }

    public boolean isDrawn() {
        return drawn;
    }

    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }

}
