package com.jcloisterzone.action;

import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.rmi.Client2ClientIF;

public abstract class FeatureAction extends PlayerAction {

	private final Sites sites;

	public FeatureAction() {
		this(new Sites());
	}
	
	public FeatureAction(Position p, Set<Location> locations) {
		this();
		sites.put(p, locations);
	}

	public FeatureAction(Sites sites) {
		this.sites = sites;
	}

	public Sites getSites() {
		return sites;
	}

	public Set<Location> getOrCreate(Position p) {
		return sites.getOrCreate(p);
	}

	public abstract void perform(Client2ClientIF server, Position p, Location d);

	@Override
	public String toString() {
		return getClass().getSimpleName() + '=' + sites.toString();
	}

}
