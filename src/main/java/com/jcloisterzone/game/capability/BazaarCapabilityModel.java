package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import io.vavr.collection.Queue;

import java.io.Serializable;

@Immutable
public class BazaarCapabilityModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Queue<BazaarItem> supply;
    private final Integer auctionedItemIndex;
    private final Player tileSelectingPlayer;

    public BazaarCapabilityModel() {
        this(null, null, null);
    }

    public BazaarCapabilityModel(Queue<BazaarItem> supply, Integer auctionedItemIndex, Player tileSelectingPlayer) {
        this.supply = supply;
        this.auctionedItemIndex = auctionedItemIndex;
        this.tileSelectingPlayer = tileSelectingPlayer;
    }

    public BazaarCapabilityModel setSupply(Queue<BazaarItem> supply) {
        return new BazaarCapabilityModel(supply, auctionedItemIndex, tileSelectingPlayer);
    }

     public BazaarCapabilityModel setAuctionedItemIndex(Integer auctionedItemIndex) {
        return new BazaarCapabilityModel(supply, auctionedItemIndex, tileSelectingPlayer);
    }

    public BazaarCapabilityModel setTileSelectingPlayer(Player tileSelectingPlayer) {
        return new BazaarCapabilityModel(supply, auctionedItemIndex, tileSelectingPlayer);
    }

    public BazaarCapabilityModel updateSupplyItem(int index, BazaarItem item) {
        return setSupply(supply.update(index,item));
    }

    public Queue<BazaarItem> getSupply() {
        return supply;
    }

    public Integer getAuctionedItemIndex() {
        return auctionedItemIndex;
    }

    public BazaarItem getAuctionedItem() {
        if (auctionedItemIndex == null) {
            return null;
        }
        return supply.get(auctionedItemIndex);
    }

    public Player getTileSelectingPlayer() {
        return tileSelectingPlayer;
    }
}
