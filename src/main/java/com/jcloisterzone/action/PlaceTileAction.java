package com.jcloisterzone.action;

import java.awt.Image;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.rmi.Client2ClientIF;

public class PlaceTileAction extends SelectTileAction {
	
	final Tile tile;
			
	public PlaceTileAction(Tile tile, Set<Position> sites) {
		super(sites);
		this.tile = tile;
	}
	
	@Override
	public Image getImage(Player player, boolean active) {
		return client.getTileTheme().getTileImage(tile.getId());
	}

	@Override
	public void perform(Client2ClientIF server, Position p) {
		throw new IllegalStateException("TODO unify");
	}

}
