package com.jcloisterzone.action;

import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;



public abstract class TileAction extends PlayerAction {

	private final Set<Position> sites;

	public TileAction() {
		this.sites = Sets.newHashSet();
	}

	public TileAction(Set<Position> sites) {
		this.sites = sites;
	}

	public Set<Position> getSites() {
		return sites;
	}

	public abstract void perform(Client2ClientIF server, Position p);

}
