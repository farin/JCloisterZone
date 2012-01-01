package com.jcloisterzone.game;

import java.util.List;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;


public interface GameDelegation {

	void initTile(Tile tile, Element xml);
	void initFeature(Tile tile, Feature feature, Element xml);
	void initPlayer(Player player);

	//TODO merge with UI events ??? - asi ne
	void begin();
	//void start();
	void prepareActions(List<PlayerAction> actions, Sites commonSites);
	void scoreCompleted(CompletableScoreContext ctx);
	void turnCleanUp();
	void finalScoring();
	//void tilePlaced(Tile tile);

	/** allow placements which are normally not allowed */
	boolean isSpecialPlacementAllowed(Tile tile, Position p);
	/** forbid placements which are normally allowed */
	boolean isPlacementAllowed(Tile tile, Position p);




}
