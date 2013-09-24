package com.jcloisterzone.game.phase;

import java.util.ArrayList;

import com.jcloisterzone.Expansion;
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

    final BazaarCapability bcb;

    public BazaarPhase(Game game, ServerIF server) {
        super(game, server);
        bcb = game.getBazaarCapability();
    }


    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS);
    }

    @Override
    public Player getActivePlayer() {
        Player bidding =  bcb.getBazaarBiddingPlayer();
        return bidding == null ? bcb.getBazaarTileSelectingPlayer() : bidding;
    }

    @Override
    public void enter() {
        if (!isBazaarTriggered()) {
            next();
            return;
        }
        Player p = game.getNextPlayer();
        bcb.setBazaarTileSelectingPlayer(p);
        game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
        if (isLocalPlayer(p)) {
            //call only from one client (from the active one)
            getServer().selectTiles(getTilePack().size(), game.getAllPlayers().length);
        }
    }

    @Override
    public void loadGame(Snapshot snapshot) {
        setEntered(true); //avoid call enter on load phase to this phase switch
        Player selecting = bcb.getBazaarTileSelectingPlayer();
        if (selecting != null) {
            Player bidding = bcb.getBazaarBiddingPlayer();
            int supplyIdx = bcb.getBazaarSupply().indexOf(bcb.getCurrentBazaarAuction());
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
        if (getTile().getTrigger() != TileTrigger.BAZAAR) return false;
        if (getTilePack().size() < game.getAllPlayers().length) return false; //there isn't one tile for each player available
        if (bcb.getBazaarSupply() != null) return false;
        return true;
    }

    @Override
    public void drawTiles(int[] tileIndexes) {
        ArrayList<BazaarItem> supply = new ArrayList<BazaarItem>(tileIndexes.length);
        for(int tileIndex : tileIndexes) {
            supply.add(new BazaarItem(getTilePack().drawTile(tileIndex)));
        }
        bcb.setBazaarSupply(supply);
        game.getUserInterface().selectBazaarTile();
    }

    private boolean canPlayerBid(Player p) {
        for(BazaarItem bi : bcb.getBazaarSupply()) {
            if (bi.getOwner() == p) return false;
        }
        return true;
    }

    @Override
    public void bazaarBid(Integer supplyIndex, Integer price) {
        BazaarItem bi = bcb.getCurrentBazaarAuction();
        boolean isTileSelection = bi == null;
        if (bi == null) {
            bi = bcb.getBazaarSupply().get(supplyIndex);
            bcb.setCurrentBazaarAuction(bi);

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
        int supplyIdx = bcb.getBazaarSupply().indexOf(bcb.getCurrentBazaarAuction());
        do {
            nextBidder = game.getNextPlayer(nextBidder);
            if (nextBidder == bcb.getBazaarTileSelectingPlayer()) {
                //all players makes bid
                BazaarItem bi = bcb.getCurrentBazaarAuction();
                if (bcb.getBazaarTileSelectingPlayer() == bi.getCurrentBidder()) {
                    bazaarBuyOrSell(true);
                } else {
                    bcb.setBazaarBiddingPlayer(bcb.getBazaarTileSelectingPlayer()); //need for correct save&load
                    game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                    game.getUserInterface().selectBuyOrSellBazaarOffer(supplyIdx);
                }
                return;
            }
        } while (!canPlayerBid(nextBidder));

        bcb.setBazaarBiddingPlayer(nextBidder);
        game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
        game.getUserInterface().makeBazaarBid(supplyIdx);
    }

    private void nextSelectingPlayer() {
        bcb.setCurrentBazaarAuction(null);
        bcb.setBazaarBiddingPlayer(null);
        Player currentSelectingPlayer = bcb.getBazaarTileSelectingPlayer();
        Player player = currentSelectingPlayer;
        do {
            player = game.getNextPlayer(player);
            if (! bcb.hasTileAuctioned(player)) {
                bcb.setBazaarTileSelectingPlayer(player);
                game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.getUserInterface().selectBazaarTile();
                return;
            }
        } while (player != currentSelectingPlayer);
        //all tiles has been auctioned
        bcb.setBazaarTileSelectingPlayer(null);
        game.fireGameEvent().bazaarAuctionsEnded();
        next();
    }

    @Override
    public void pass() {
        if (bcb.getBazaarBiddingPlayer() == bcb.getBazaarTileSelectingPlayer()) {
            logger.error("Tile selecting player is not allowed to pass");
            return;
        }
        nextBidder();
    }

    @Override
    public void bazaarBuyOrSell(boolean buy) {
        BazaarItem bi = bcb.getCurrentBazaarAuction();
        int points = bi.getCurrentPrice();
        Player pSelecting = bcb.getBazaarTileSelectingPlayer();
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
