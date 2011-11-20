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
import com.jcloisterzone.game.PlayerSlot;
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
	protected PlayerSlot[] initPlayerSlots(Game game) {
		return new PlayerSlot[game.getAllPlayers().length];
	}

	@Override
	protected void preparePlayers() {
		//update plain (created by snapshot) players's slot with real one from dialog
		for(int i = 0; i < slots.length; i++) {
			for(Player p: game.getAllPlayers()) {
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
		for(Iterator<Tile> iter = ((DefaultTilePack)getTilePack()).drawPrePlacedActiveTiles().iterator(); iter.hasNext();) {
			Tile preplaced = iter.next();
			game.getBoard().add(preplaced, preplaced.getPosition(), true);
			game.fireGameEvent().tilePlaced(preplaced);
		}
		for(Meeple m : tilePackFactory.getPreplacedMeeples()) {
			Tile tile = game.getBoard().get(m.getPosition());
			Feature f;
			if (m instanceof Barn) {
				//special case, barn holds 'corner' location
				f = tile.getFeaturePartOf(m.getLocation());
			} else {
				f = tile.getFeature(m.getLocation());
			}
			m.setFeature(f);
			f.setMeeple(m);
			game.fireGameEvent().deployed(m);
		}
		snapshot.loadExpansionData(game);
	}

	@Override
	protected void prepareTilePack() {
		tilePackFactory = new LoadGameTilePackFactory();
		tilePackFactory.setGame(game);
		tilePackFactory.setExpansions(game.getExpansions());
		tilePackFactory.setSnapshot(snapshot);
		game.setTilePack(tilePackFactory.createTilePack());
		for(String tileId : snapshot.getDiscardedTiles()) {
			game.getBoard().discardTile(tileId);
		}
	}

	@Override
	public void next() {
		if (getDefaultNext() instanceof TilePhase) {
			//tile drawn but not placed yet
			String tileId = snapshot.getNextTile();
			Tile tile = game.getTilePack().drawTile(tileId);
			game.getBoard().checkMoves(tile);
			game.fireGameEvent().tileDrawn(tile);
		}
		super.next();
	}
}
