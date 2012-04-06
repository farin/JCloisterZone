package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public class PrincessAction extends SelectFeatureAction {

	@Override
	public void perform(Client2ClientIF server, Position p, Location d) {
		server.undeployMeeple(p, d);
	}

	@Override
	protected int getSortOrder() {
		return 1;
	}


}
