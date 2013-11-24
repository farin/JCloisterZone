package com.jcloisterzone.game.phase;

import java.util.ArrayList;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.rmi.ServerIF;

public class BazaarPhase extends ServerAwarePhase {

    private final BazaarCapability bazaarCap;

    public BazaarPhase(Game game, ServerIF server) {
        super(game, server);
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
        game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
        if (isLocalPlayer(p)) {
            //call only from one client (from the active one)
            getServer().selectTiles(getTilePack().size(), game.getAllPlayers().length);
        }
    }

    @Override
    public void loadGame(Snapshot snapshot) {
        setEntered(true); //avoid call enter on load phase to this phase switch
        Player selecting = bazaarCap.getBazaarTileSelectingPlayer();
        if (selecting != null) {
            Player bidding = bazaarCap.getBazaarBiddingPlayer();
            int supplyIdx = bazaarCap.getBazaarSupply().indexOf(bazaarCap.getCurrentBazaarAuction());
            game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());

            if (bidding == null) {
                game.getUserInterface().selectBazaarTile();
            } else if (selecting == bidding) {
                game.getUserInterface().selectBuyOrSellBazaarOffer(supplyIdx);
            } else {
                game.getUserInterface().makeBazaarBid(supplyIdx);
            }
        }
    }

    private boolean isBazaarTriggered() {
        if (!getTile().hasTrigger(TileTrigger.BAZAAR)) return false;
        if (getTilePack().size() < game.getAllPlayers().length) return false; //there isn't one tile for each player available
        if (bazaarCap.getBazaarSupply() != null) return false;
        return true;
    }

    @Override
    public void drawTiles(int[] tileIndexes) {
        ArrayList<BazaarItem> supply = new ArrayList<BazaarItem>(tileIndexes.length);
        for (int tileIndex : tileIndexes) {
            supply.add(new BazaarItem(getTilePack().drawTile(tileIndex)));
        }
        bazaarCap.setBazaarSupply(supply);
        game.getUserInterface().selectBazaarTile();
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
            game.fireGameEvent().bazaarTileSelected(supplyIndex, bi);
        }
        nextBidder();
    }

    private void nextBidder() {
        Player nextBidder = getActivePlayer();
        int supplyIdx = bazaarCap.getBazaarSupply().indexOf(bazaarCap.getCurrentBazaarAuction());
        do {
            nextBidder = game.getNextPlayer(nextBidder);
            if (nextBidder == bazaarCap.getBazaarTileSelectingPlayer()) {
                //all players makes bid
                BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
                if (bazaarCap.getBazaarTileSelectingPlayer() == bi.getCurrentBidder()) {
                    bazaarBuyOrSell(true);
                } else {
                    bazaarCap.setBazaarBiddingPlayer(bazaarCap.getBazaarTileSelectingPlayer()); //need for correct save&load
                    game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                    game.getUserInterface().selectBuyOrSellBazaarOffer(supplyIdx);
                }
                return;
            }
        } while (!canPlayerBid(nextBidder));

        bazaarCap.setBazaarBiddingPlayer(nextBidder);
        game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
        game.getUserInterface().makeBazaarBid(supplyIdx);
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
                game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.getUserInterface().selectBazaarTile();
                return;
            }
        } while (player != currentSelectingPlayer);
        //all tiles has been auctioned
        bazaarCap.setBazaarTileSelectingPlayer(null);
        game.fireGameEvent().bazaarAuctionsEnded();
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
