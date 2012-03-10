package com.jcloisterzone.game.phase;

import java.util.List;

import com.google.common.collect.Lists;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.BridgesCastlesBazaarsGame;


public class ActionPhase extends Phase {


	public ActionPhase(Game game) {
		super(game);
	}

	@Override
	public void enter() {
		List<PlayerAction> actions = Lists.newArrayList();

		Sites commonSites = game.prepareCommonSites();
		if (getActivePlayer().hasFollower(SmallFollower.class)  && ! commonSites.isEmpty()) {
			actions.add(new MeepleAction(SmallFollower.class, commonSites));
		}
		game.expansionDelegate().prepareActions(actions, commonSites);
		//TODO mayor a wagon action !!! be careful about magic gate !
		if (isAutoTurnEnd(actions)) {
			next();
		} else {
			notifyUI(actions, true);
		}
	}

	private boolean isAutoTurnEnd(List<PlayerAction> actions) {
		if (! actions.isEmpty()) return false;
		if (game.hasExpansion(Expansion.TOWER)) {
			if (game.getTowerGame().isAnyFollowerImprisoned(getActivePlayer())) {
				//player can return figure immediately
				return false;
			}
		}
		return true;
	}

	@Override
	public void pass() {
		next();
	}

	private int doPlaceTowerPiece(Position p) {
		Tower tower = getBoard().get(p).getTower();
		if (tower  == null) {
			throw new IllegalArgumentException("No tower on tile.");
		}
		if (tower.getMeeple() != null) {
			throw new IllegalArgumentException("The tower is sealed");
		}
		game.getTowerGame().decreaseTowerPieces(getActivePlayer());
		return tower.increaseHeight();
	}

	public CaptureAction prepareCapture(Position p, int range) {
		CaptureAction captureAction = new CaptureAction();
		for(Meeple pf : game.getDeployedMeeples()) {
			if (! (pf instanceof Follower)) continue;
			if (pf.getPosition().x != p.x && pf.getPosition().y != p.y) continue; //check if is in same row or column
			if (pf.getPosition().squareDistance(p) > range) continue;
			captureAction.getOrCreate(pf.getPosition()).add(pf.getLocation());
		}
		return captureAction;
	}

	@Override
	public void placeTowerPiece(Position p) {
		int captureRange = doPlaceTowerPiece(p);
		game.fireGameEvent().towerIncreased(p, captureRange);
		CaptureAction captureAction = prepareCapture(p, captureRange);
		if (captureAction.getSites().isEmpty()) {
			next();
			return;
		}
		next(TowerCapturePhase.class);
		notifyUI(captureAction, false);		
	}

	@Override
	public void moveFairy(Position p) {
		for(Follower f : game.getActivePlayer().getFollowers()) {
			if (p.equals(f.getPosition())) {
				game.getPrincessAndDragonGame().setFairyPosition(p);
				game.fireGameEvent().fairyMoved(p);
				next();
				return;
			}
		}
		throw new IllegalArgumentException("No own follower on the tile");
	}

	@Override
	public void removeKnightWithPrincess(Position p, Location loc) {
		Meeple m = game.getMeeple(p, loc);
		if (! (m.getFeature() instanceof City)) {
			throw new IllegalArgumentException("Feature must be a city");
		}
		m.undeploy();
		next();
	}

	@Override
	public void placeTunnelPiece(Position p, Location loc, boolean isB) {
		game.getTunnelGame().placeTunnelPiece(p, loc, isB);
		next(ActionPhase.class);
	}


	@Override
	public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
		Meeple m = getActivePlayer().getUndeployedMeeple(meepleType);
		m.deploy(getBoard().get(p), loc);
		next();
	}
	
	@Override
	public void deployBridge(Position pos, Location loc) {
		BridgesCastlesBazaarsGame bcb = game.getBridgesCastlesBazaarsGame();
		bcb.decreaseBridges(getActivePlayer());		
		bcb.deployBridge(pos, loc);
		next(ActionPhase.class);
	}

}
