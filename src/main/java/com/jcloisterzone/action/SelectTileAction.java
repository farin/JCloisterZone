package com.jcloisterzone.action;

import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;


public abstract class SelectTileAction extends PlayerAction {

	private final Set<Position> sites;

	public SelectTileAction() {
		this.sites = Sets.newHashSet();
	}

	public SelectTileAction(Set<Position> sites) {
		this.sites = sites;
	}

	public Set<Position> getSites() {
		return sites;
	}

	public abstract void perform(Client2ClientIF server, Position p);

}
