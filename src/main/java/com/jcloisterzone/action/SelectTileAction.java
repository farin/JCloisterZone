package com.jcloisterzone.action;

import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;


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
	
	@Override
	protected GridLayer createGridLayer() {
		return new TileActionLayer(client.getGridPanel(), this);
	}

	public abstract void perform(Client2ClientIF server, Position p);

}
