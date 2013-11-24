package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;

public class BazaarItem {

    private final Tile tile;
    private Player owner;

    private int currentPrice;
    private Player currentBidder;


    public BazaarItem(Tile tile) {
        this.tile = tile;
    }

    public BazaarItem(BazaarItem bi) {
        tile = bi.tile;
        owner = bi.owner;
        currentPrice = bi.currentPrice;
        currentBidder = bi.currentBidder;
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

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Player getCurrentBidder() {
        return currentBidder;
    }

    public void setCurrentBidder(Player currentBidder) {
        this.currentBidder = currentBidder;
    }

    @Override
    public String toString() {
        return "BazaarItem [tile=" + tile + ", owner=" + owner
                + ", currentPrice=" + currentPrice + ", currentBidder="
                + currentBidder + "]";
    }

}
