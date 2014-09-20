package com.jcloisterzone.game.phase;

import java.util.ArrayList;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.BazaarAuctionEndEvent;
import com.jcloisterzone.event.BazaarMakeBidEvent;
import com.jcloisterzone.event.BazaarSelectBuyOrSellEvent;
import com.jcloisterzone.event.BazaarSelectTileEvent;
import com.jcloisterzone.event.BazaarTileSelectedEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.MakeDrawMessage;
import com.jcloisterzone.wsio.message.DrawMessage;

public class BazaarPhase extends ServerAwarePhase {

    private final BazaarCapability bazaarCap;

    public BazaarPhase(Game game, Connection conn) {
        super(game, conn);
        bazaarCap = game.getCapability(BazaarCapability.class);
    }


    @Override
    public boolean isActive() {
        return game.hasCapability(BazaarCapability.class);
    }

    @Override
    public Player getActivePlayer() {
        Player bidding =  bazaarCap.getBazaarBiddingPlayer();
        return bidding == null ? bazaarCap.getBazaarTileSelectingPlayer() : bidding;
    }

    @Override
    public void enter() {
        if (!isBazaarTriggered()) {
            next();
            return;
        }
        Player p = game.getNextPlayer();
        bazaarCap.setBazaarTileSelectingPlayer(p);
        //game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
        if (isLocalPlayer(p)) {
            //call only from one client (from the active one)
            getConnection().send(new MakeDrawMessage(game.getGameId(), getTilePack().size(), game.getAllPlayers().length));
        }
    }

    @Override
    public void loadGame(Snapshot snapshot) {
        setEntered(true); //avoid call enter on load phase to this phase switch
        Player selecting = bazaarCap.getBazaarTileSelectingPlayer();
        if (selecting != null) {
            Player bidding = bazaarCap.getBazaarBiddingPlayer();
            BazaarItem currentItem = bazaarCap.getCurrentBazaarAuction();
            int supplyIdx = bazaarCap.getBazaarSupply().indexOf(currentItem);
            //game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());

            if (bidding == null) {
                game.post(new BazaarSelectTileEvent(getActivePlayer(), bazaarCap.getBazaarSupply()));
            } else if (selecting == bidding) {
                game.post(new BazaarSelectBuyOrSellEvent(bidding, currentItem, supplyIdx));
            } else {
                game.post(new BazaarMakeBidEvent(bidding, currentItem, supplyIdx));
            }
        }
    }

    private boolean isBazaarTriggered() {
        if (!bazaarCap.isBazaarTriggered()) return false;
        if (getTilePack().size() < game.getAllPlayers().length) return false; //there isn't one tile for each player available
        if (bazaarCap.getBazaarSupply() != null) return false;
        return true;
    }

    @WsSubscribe
    public void handleRandSample(DrawMessage msg) {
        int size = game.getAllPlayers().length;
        if (msg.getPackSize() != getTilePack().size() || msg.getValues().length != size) {
            logger.error("Invalid message");
            return;
        }
        ArrayList<BazaarItem> supply = new ArrayList<BazaarItem>(size);
        for (Tile t : getTilePack().drawTiles(msg.getValues())) {
            supply.add(new BazaarItem(t));
        }
        bazaarCap.setBazaarSupply(supply);
        game.post(new BazaarSelectTileEvent(getActivePlayer(), supply));
    }

    private boolean canPlayerBid(Player p) {
        for (BazaarItem bi : bazaarCap.getBazaarSupply()) {
            if (bi.getOwner() == p) return false;
        }
        return true;
    }

    @Override
    public void bazaarBid(Integer supplyIndex, Integer price) {
        BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
        boolean isTileSelection = bi == null;
        if (bi == null) {
            bi = bazaarCap.getBazaarSupply().get(supplyIndex);
            bazaarCap.setCurrentBazaarAuction(bi);

            if (game.hasRule(CustomRule.BAZAAR_NO_AUCTION)) {
                bi.setOwner(getActivePlayer());
                nextSelectingPlayer();
                return;
            }
        }
        bi.setCurrentPrice(price);
        bi.setCurrentBidder(getActivePlayer());

        if (isTileSelection) {
            game.post(new BazaarTileSelectedEvent(getActivePlayer(), bi, supplyIndex));
        }
        nextBidder();
    }

    private void nextBidder() {
        Player nextBidder = getActivePlayer();
        BazaarItem currentItem = bazaarCap.getCurrentBazaarAuction();
        int supplyIdx = bazaarCap.getBazaarSupply().indexOf(currentItem);
        do {
            nextBidder = game.getNextPlayer(nextBidder);
            if (nextBidder == bazaarCap.getBazaarTileSelectingPlayer()) {
                //all players makes bid
                BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
                if (bazaarCap.getBazaarTileSelectingPlayer() == bi.getCurrentBidder()) {
                    bazaarBuyOrSell(true);
                } else {
                    bazaarCap.setBazaarBiddingPlayer(bazaarCap.getBazaarTileSelectingPlayer()); //need for correct save&load
                    //game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                    game.post(new BazaarSelectBuyOrSellEvent(getActivePlayer(), currentItem, supplyIdx));
                }
                return;
            }
        } while (!canPlayerBid(nextBidder));

        bazaarCap.setBazaarBiddingPlayer(nextBidder);
        //game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
        game.post(new BazaarMakeBidEvent(getActivePlayer(), currentItem, supplyIdx));
    }

    private void nextSelectingPlayer() {
        bazaarCap.setCurrentBazaarAuction(null);
        bazaarCap.setBazaarBiddingPlayer(null);
        Player currentSelectingPlayer = bazaarCap.getBazaarTileSelectingPlayer();
        Player player = currentSelectingPlayer;
        do {
            player = game.getNextPlayer(player);
            if (!bazaarCap.hasTileAuctioned(player)) {
                bazaarCap.setBazaarTileSelectingPlayer(player);
                //game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.post(new BazaarSelectTileEvent(getActivePlayer(), bazaarCap.getBazaarSupply()));
                return;
            }
        } while (player != currentSelectingPlayer);
        //all tiles has been auctioned
        bazaarCap.setBazaarTileSelectingPlayer(null);
        game.post(new BazaarAuctionEndEvent());
        next();
    }

    @Override
    public void pass() {
        if (bazaarCap.getBazaarBiddingPlayer() == bazaarCap.getBazaarTileSelectingPlayer()) {
            logger.error("Tile selecting player is not allowed to pass");
            return;
        }
        nextBidder();
    }

    @Override
    public void bazaarBuyOrSell(boolean buy) {
        BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
        int points = bi.getCurrentPrice();
        Player pSelecting = bazaarCap.getBazaarTileSelectingPlayer();
        Player pBidding = bi.getCurrentBidder();

        assert pSelecting != pBidding || buy; //if same, buy is flag expected
        if (!buy) points *= -1;
        pSelecting.addPoints(-points, PointCategory.BAZAAR_AUCTION);
        if (pSelecting != pBidding) {
            pBidding.addPoints(points, PointCategory.BAZAAR_AUCTION);
        }

        bi.setOwner(buy ? pSelecting : pBidding);
        bi.setCurrentBidder(null);
        nextSelectingPlayer();
    }


}
