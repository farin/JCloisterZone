package com.jcloisterzone.action;

import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;


public class TowerPieceAction extends TileAction {

	public TowerPieceAction() {
		super();
	}

	public TowerPieceAction(Set<Position> sites) {
		super(sites);
	}

	@Override
	public void perform(Client2ClientIF server, Position p) {
		server.placeTowerPiece(p);
	}

	@Override
	protected int getSortOrder() {
		return 20;
	}

}
