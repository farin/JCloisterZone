package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.io.message.BazaarBuyOrSellMessage.BuyOrSellOption;

public class TileAuctionedEvent extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private final Tile tile;
    private final int points;
    private final BuyOrSellOption option;
    private final Player auctioneer;
    private final Player bidder;

    public TileAuctionedEvent(PlayEventMeta metadata, Tile tile, BuyOrSellOption option, int points, Player auctioneer, Player bidder) {
        super(metadata);
        this.tile = tile;
        this.option = option;
        this.points = points;
        this.auctioneer = auctioneer;
        this.bidder = bidder;
    }

    public Tile getTile() {
        return tile;
    }

    public BuyOrSellOption getOption() {
        return option;
    }

    public int getPoints() {
        return points;
    }

    public Player getAuctioneer() {
        return auctioneer;
    }

    public Player getBidder() {
        return bidder;
    }
}
