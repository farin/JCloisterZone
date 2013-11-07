package com.jcloisterzone.game.phase;

import java.util.Iterator;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.LoadGameTilePackFactory;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.rmi.ServerIF;

public class LoadGamePhase extends CreateGamePhase {

    private Snapshot snapshot;
    private LoadGameTilePackFactory tilePackFactory;

    public LoadGamePhase(Game game, Snapshot snapshot, ServerIF server) {
        super(game, server);
        this.snapshot = snapshot;
    }

    @Override
    protected Snapshot getSnapshot() {
        return snapshot;
    }

    @Override
    protected void preparePlayers() {
        //update plain (created by snapshot) players's slot with real one from dialog
        for (int i = 0; i < slots.length; i++) {
            for (Player p: game.getAllPlayers()) {
                if (p.getSlot().getNumber() == i) {
                    p.setSlot(slots[i]);
                }
            }
        }
        initializePlayersMeeples();
    }


    @Override
    protected void preparePhases() {
        super.preparePhases();
        Phase active = game.getPhases().get(snapshot.getActivePhase());
        setDefaultNext(active);
    }

    @Override
    protected void preplaceTiles() {
        //super.preplaceTiles();
        for (Iterator<Tile> iter = ((DefaultTilePack)getTilePack()).drawPrePlacedActiveTiles().iterator(); iter.hasNext();) {
            Tile preplaced = iter.next();
            game.getBoard().add(preplaced, preplaced.getPosition(), true);
            game.getBoard().mergeFeatures(preplaced);
            game.fireGameEvent().tilePlaced(preplaced);
            if (preplaced.getBridge() != null) {
                game.fireGameEvent().bridgeDeployed(preplaced.getPosition(), preplaced.getBridge().getLocation());
            }
        }
        for (Meeple m : tilePackFactory.getPreplacedMeeples()) {
            Tile tile = game.getBoard().get(m.getPosition());
            Feature f;
            if (m instanceof Barn) {
                //special case, barn holds 'corner' location
                f = tile.getFeaturePartOf(m.getLocation());
            } else {
                f = tile.getFeature(m.getLocation());
            }
            m.setFeature(f);
            f.addMeeple(m);
            game.fireGameEvent().deployed(m);
        }
        tilePackFactory.activateGroups((DefaultTilePack) game.getTilePack());
        snapshot.loadCapabilities(game);
    }

    @Override
    protected void prepareTilePack() {
        tilePackFactory = new LoadGameTilePackFactory();
        tilePackFactory.setGame(game);
        tilePackFactory.setConfig(game.getConfig());
        tilePackFactory.setExpansions(game.getExpansions());
        tilePackFactory.setSnapshot(snapshot);
        DefaultTilePack tilePack = tilePackFactory.createTilePack();
        game.setTilePack(tilePack);
        for (String tileId : snapshot.getDiscardedTiles()) {
            Tile tile = tilePack.drawTile(tileId);
            game.getBoard().discardTile(tile);
        }
    }

    @Override
    public void next() {
        super.next();
        getDefaultNext().loadGame(snapshot); //call after super.next() to be able fake entered flag
    }
}
