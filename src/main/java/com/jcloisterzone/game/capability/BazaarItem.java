package com.jcloisterzone.game.capability;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;

@Immutable
public class BazaarItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Tile tile;
    private final int currentPrice;
    private final Player currentBidder;
    private final Player owner;

    public BazaarItem(Tile tile, int currentPrice, Player currentBidder, Player owner) {
        this.tile = tile;
        this.currentPrice = currentPrice;
        this.currentBidder = currentBidder;
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public BazaarItem setOwner(Player owner) {
        return new BazaarItem(tile, currentPrice, currentBidder, owner);
    }

    public Tile getTile() {
        return tile;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public BazaarItem setCurrentPrice(int currentPrice) {
        return new BazaarItem(tile, currentPrice, currentBidder, owner);
    }

    public Player getCurrentBidder() {
        return currentBidder;
    }

    public BazaarItem setCurrentBidder(Player currentBidder) {
        return new BazaarItem(tile, currentPrice, currentBidder, owner);
    }

    @Override
    public String toString() {
        return "BazaarItem [tile=" + tile + ", owner=" + owner
                + ", currentPrice=" + currentPrice + ", currentBidder="
                + currentBidder + "]";
    }

}
