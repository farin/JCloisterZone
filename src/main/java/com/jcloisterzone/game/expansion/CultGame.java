package com.jcloisterzone.game.expansion;

import static com.jcloisterzone.board.XmlUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.game.ExpandedGame;


public final class CultGame extends ExpandedGame {


	@Override
	public void initFeature(Tile tile, Feature feature, Element xml) {
		if (feature instanceof Cloister) {
			((Cloister)feature).setShrine(attributeBoolValue(xml, "shrine"));
		}
	}

	public void makeCloisterChallenged(Cloister cloister) {
		if (cloister.getMeeple() == null) return;
		getGame().fireGameEvent().scored(cloister, 0, "0", cloister.getMeeple(), false);
		cloister.getMeeple().undeploy();
	}


	public void resolveChallengedCloisters(Cloister cloister) {
		Position p = cloister.getTile().getPosition();
		for(Tile nt : game.getBoard().getAllNeigbourTiles(p)) {
			if (nt.hasCloister()) {
				Cloister nextCloister = nt.getCloister();
				if (cloister.isShrine() ^ nextCloister.isShrine()) {
					//opposite cloisters
					if ( ! nextCloister.isFeatureCompleted()) {
						makeCloisterChallenged(nextCloister);
					}
				}
			}
		}
	}

	@Override
	public void scoreCompleted(CompletableScoreContext ctx) {
		if (ctx.getMasterFeature() instanceof Cloister) {
			resolveChallengedCloisters((Cloister) ctx.getMasterFeature());
		}
	}

	@Override
	public boolean isPlacementAllowed(Tile tile, Position p) {
		if (tile.hasCloister()) {
			int opositeCount = 0;
			int sameCount = 0;
			for(Tile nt: getBoard().getAllNeigbourTiles(p)) {
				if (nt.hasCloister()) {
					if  (tile.getCloister().isShrine() ^ nt.getCloister().isShrine()) {
						opositeCount++;
					} else {
						sameCount++;
					}
				}
			}
			if (opositeCount > 1 || (opositeCount == 1 && sameCount > 0)) {
				return false;
			}
		}
		return true;
	}

}
