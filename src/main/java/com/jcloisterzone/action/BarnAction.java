package com.jcloisterzone.action;

import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.rmi.Client2ClientIF;


public class BarnAction extends PlayerAction {

	private final Set<Location> sites = Sets.newHashSet();

	public Set<Location> getSites() {
		return sites;
	}

	public void perform(Client2ClientIF server, Position p, Location d) {
		server.deployMeeple(p, d, Barn.class);
	}

	@Override
	public String getName() {
		return "barn";
	}

	@Override
	protected int getSortOrder() {
		return 9;
	}
}
