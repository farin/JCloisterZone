package com.jcloisterzone.game.phase;

import java.util.List;

import com.google.common.collect.Lists;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class ScorePhase extends Phase {

	private List<Completable> alredyScored = Lists.newArrayList();

	public ScorePhase(Game game) {
		super(game);
	}

	private void scoreCompletedOnTile(Tile tile) {
		for(Feature feature : tile.getFeatures()) {
			if (feature instanceof Completable) {
				scoreCompleted((Completable) feature);
			}
		}
	}

	private void scoreCompletedNearAbbey(Position pos) {
		for(Position offset: Position.ADJACENT.values()) {
			Tile tile = getBoard().get(pos.add(offset));
			for(Feature feature : tile.getFeatures()) {
				//must skip because cloister are check later
				//and double trigger is not wanted
				if (feature instanceof Cloister) continue;
				if (feature instanceof Completable) {
					scoreCompleted((Completable) feature);
				}
			}
		}
	}

	@Override
	public void enter() {
		Position pos = getTile().getPosition();

		scoreCompletedOnTile(getTile());
		if (getTile().isAbbeyTile()) {
			scoreCompletedNearAbbey(pos);
		}

		if (game.hasExpansion(Expansion.TUNNEL)) {
			Road r = game.getTunnelGame().getPlacedTunnel();
			if (r != null) {
				scoreCompleted(r);
			}
		}

		alredyScored.clear();

		for(Tile neighbour : getBoard().getAllNeigbourTiles(pos)) {
			Cloister cloister = neighbour.getCloister();
			if (cloister != null) {
				scoreCompleted(cloister);
			}
		}

		next();
	}

	protected void undeployMeeples(CompletableScoreContext ctx) {
		for(Meeple m : ctx.getMeeples()) {
			m.undeploy(false);
		}
	}

	private void scoreCompleted(Completable completable) {
		CompletableScoreContext ctx = completable.getScoreContext();
		completable.walk(ctx);
		if (game.hasExpansion(Expansion.TRADERS_AND_BUILDERS)) {
			for(Meeple m : ctx.getSpecialMeeples()) {
				if (m instanceof Builder && m.getPlayer().equals(game.getActivePlayer())) {
					if (! m.getPosition().equals(getTile().getPosition())) {
						game.getTradersAndBuildersGame().builderUsed();
					}
					break;
				}
			}
		}
		if (ctx.isCompleted()) {
			Completable master = (Completable) ctx.getMasterFeature();
			if (! alredyScored.contains(master)) {
				alredyScored.add(master);
				game.expansionDelegate().scoreCompleted(ctx);
				game.scoreCompletableFeature(ctx);
				undeployMeeples(ctx);
				game.fireGameEvent().completed(master, ctx);
			}
		}
	}

}
