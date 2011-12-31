package com.jcloisterzone.rmi;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;

public interface Client2ClientIF {

	/* ---------------------- NEW GAME MESSAGES ------------------*/

	public void updateExpansion(Expansion expansion, Boolean enabled);
	public void updateCustomRule(CustomRule rule, Boolean enabled);
	public void startGame();

	/* ---------------------- STARTED GAME MESSAGES ------------------*/

	public void placeNoFigure();
	public void placeNoTile();

	public void placeTile(Rotation rotation, Position position);

	public void deployMeeple(Position pos, Location loc, Class<? extends Meeple> meepleType);
	public void placeTowerPiece(Position pos);
	public void escapeFromCity(Position pos, Location loc);
	public void removeKnightWithPrincess(Position pos, Location loc);
	public void captureFigure(Position pos, Location loc);
	public void placeTunnelPiece(Position pos, Location loc, boolean isSecondPiece);

	public void moveFairy(Position pos);
	public void moveDragon(Position pos);

	public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType);
	
	public void deployBridge(Position pos, Location loc); 

}
