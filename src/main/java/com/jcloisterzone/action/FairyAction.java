package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public class FairyAction extends TileAction {

	@Override
	public void perform(Client2ClientIF server, Position p) {
		server.moveFairy(p);
	}

	@Override
	protected int getSortOrder() {
		return 30;
	}


}
