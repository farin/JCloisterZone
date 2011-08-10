package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.rmi.Client2ClientIF;

public class TunnelAction extends FeatureAction {

	private final boolean secondTunnelPiece;

	public TunnelAction(boolean secondTunnelPiece, Sites sites) {
		super(sites);
		this.secondTunnelPiece = secondTunnelPiece;
	}

	public boolean isSecondTunnelPiece() {
		return secondTunnelPiece;
	}

	@Override
	public void perform(Client2ClientIF server, Position p, Location d) {
		server.placeTunnelPiece(p, d, secondTunnelPiece);

	}

	@Override
	protected int getSortOrder() {
		return secondTunnelPiece ? 41 : 40;
	}

}
