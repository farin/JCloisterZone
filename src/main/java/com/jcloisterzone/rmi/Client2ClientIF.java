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

	public void deployMeeple(Position p, Location d, Class<? extends Meeple> meepleType);
	public void placeTowerPiece(Position p);
	public void escapeFromCity(Position p, Location d);
	public void removeKnightWithPrincess(Position p, Location d);
	public void captureFigure(Position p, Location d);
	public void placeTunnelPiece(Position p, Location d, boolean isSecondPiece);

	public void moveFairy(Position p);
	public void moveDragon(Position p);

	public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType);

}
