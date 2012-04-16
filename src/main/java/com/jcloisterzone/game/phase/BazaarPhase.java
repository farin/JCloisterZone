package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
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
    public void enter() {
        if (!isBazaarTriggered()) {
            next();
            return;
        }
        if (isLocalPlayer(game.getActivePlayer())) {
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
}
