package com.jcloisterzone.game.phase;

import java.util.Arrays;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
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
        return bcb.getBazaarBiddingPlayer();
    }

    @Override
    public void enter() {
        if (!isBazaarTriggered()) {
            next();
            return;
        }
        Player p = game.getNextPlayer();
        bcb.setBazaarTileSelectingPlayer(p);
        bcb.setBazaarBiddingPlayer(p);
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
    public void drawTiles(Integer[] tileIndexes) {
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
        if (bcb.getCurrentBazaarAuction() == null) {
            //active player just selected tile
            BazaarItem bi = bcb.getBazaarSupply()[supplyIndex];
            bcb.setCurrentBazaarAuction(bi);
            game.fireGameEvent().bazaarTileSelected(supplyIndex, bi);
        }
        nextBidder();
    }

    private void nextBidder() {
        Player nextBidder = getActivePlayer();
        do {
            nextBidder = game.getNextPlayer(nextBidder);
            if (nextBidder == bcb.getBazaarTileSelectingPlayer()) {
                //all players makes bid
                return;
            }
        } while (!canPlayerBid(nextBidder));

        BazaarItem[] supply = bcb.getBazaarSupply();
        for(int i = 0; i < supply.length; i++) {
            if (supply[i] == bcb.getCurrentBazaarAuction()) {
                game.getUserInterface().makeBazaarBid(i);
                break;
            }
        }
    }

    @Override
    public void pass() {
        if (bcb.getBazaarBiddingPlayer() == bcb.getBazaarTileSelectingPlayer()) {
            logger.error("Tile selecting player is not allowed to pass");
            return;
        }
        nextBidder();
    }
}
