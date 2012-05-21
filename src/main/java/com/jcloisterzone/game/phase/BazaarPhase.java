package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BazaarItem;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;
import com.jcloisterzone.rmi.ServerIF;

public class BazaarPhase extends ServerAwarePhase {

    final BridgesCastlesBazaarsGame bcb;

    public BazaarPhase(Game game, ServerIF server) {
        super(game, server);
        bcb = game.getBridgesCastlesBazaarsGame();
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

    private boolean isBazaarTriggered() {
        if (getTile().getTrigger() != TileTrigger.BAZAAR) return false;
        if (getTilePack().size() < game.getAllPlayers().length) return false; //there isn't one tile for each player available
        if (bcb.getBazaarSupply() != null) return false;
        return true;
    }

    @Override
    public void drawTiles(int[] tileIndexes) {
        BazaarItem[] supply = new BazaarItem[tileIndexes.length];
        for(int i = 0; i < tileIndexes.length; i++) {
            supply[i] = new BazaarItem(getTilePack().drawTile(tileIndexes[i]));
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
            bi = bcb.getBazaarSupply()[supplyIndex];
            bcb.setCurrentBazaarAuction(bi);
        }
        bi.setCurrentPrice(price);
        bi.setCurrentBidder(getActivePlayer());

        if (isTileSelection) {
            game.fireGameEvent().bazaarTileSelected(supplyIndex, bi);
        }
        nextBidder();
    }

    private int getCurrentBazaarAuctionIndex() {
        BazaarItem[] supply = bcb.getBazaarSupply();
        for(int i = 0; i < supply.length; i++) {
            if (supply[i] == bcb.getCurrentBazaarAuction()) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    private void nextBidder() {
        Player nextBidder = getActivePlayer();
        int supplyIdx = getCurrentBazaarAuctionIndex();
        do {
            nextBidder = game.getNextPlayer(nextBidder);
            if (nextBidder == bcb.getBazaarTileSelectingPlayer()) {
                //all players makes bid
                bcb.setBazaarBiddingPlayer(null);
                BazaarItem bi = bcb.getCurrentBazaarAuction();
                if (bcb.getBazaarTileSelectingPlayer() == bi.getCurrentBidder()) {
                    bazaarBuyOrSell(true);
                } else {
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
        Player currentPlayer = bcb.getBazaarTileSelectingPlayer();
        Player nextPlayer = game.getNextPlayer(currentPlayer);
        while (nextPlayer != currentPlayer) {
            if (! bcb.hasTileAuctioned(nextPlayer)) {
                bcb.setBazaarTileSelectingPlayer(nextPlayer);
                game.fireGameEvent().playerActivated(game.getTurnPlayer(), getActivePlayer());
                game.getUserInterface().selectBazaarTile();
                return;
            }
            nextPlayer = game.getNextPlayer(nextPlayer);
        }
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
