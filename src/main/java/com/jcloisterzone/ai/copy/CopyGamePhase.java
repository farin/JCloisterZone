package com.jcloisterzone.ai.copy;

import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.LoadGamePhase;

@Deprecated // TODEL
public class CopyGamePhase extends LoadGamePhase {

	//private TilePack originalTilePack;

	public CopyGamePhase(Game game, Snapshot snapshot, TilePack originalTilePack) {
		super(game, snapshot, null);
		//this.originalTilePack = originalTilePack;
	}

	@Override
	protected void preparePlayers() {
		initializePlayersMeeples();
	}

	@Override
	protected void prepareAiPlayers() {
		//empty
	}
	
//	protected void preplaceTiles() {		
//	}
//
//	@Override
//	protected void prepareTilePack() {
//		TilePack tilePack = new TilePackCopy(originalTilePack, game);
//		game.setTilePack(tilePack);
//	}
}