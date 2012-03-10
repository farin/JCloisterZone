package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.GridLayer;

public abstract class PlayerAction implements Comparable<PlayerAction> {
	
	protected Client client;
	private GridLayer gridLayer;
	
	public String getName() {
		return getClass().getSimpleName().toLowerCase().replace("action", "");
	}
	
	public Image getImage(Player player, boolean active) {					
		return getImage(active ? client.getPlayerColor(player) : Color.GRAY);		
	}
	
	protected GridLayer createGridLayer() { 
		return null;
	}
	
	/** Called when user select action in action panel */
	public void select() {
		gridLayer = createGridLayer();
		if (gridLayer != null) {
			client.getGridPanel().addLayer(gridLayer);
		}
	}
	
	/** Called when user deselect action in action panel */
	public void deselect() { 
		if (gridLayer != null) {
			client.getGridPanel().clearActionDecorations();
			gridLayer = null;
		}
	}
	
		
	protected Image getImage(Color color) {
		return client.getFigureTheme().getActionImage(this, color);
	}
	
	
	protected int getSortOrder() {
		return 1024;
	}

	@Override
	public int compareTo(PlayerAction o) {
		return getSortOrder() - o.getSortOrder();
	}
	
	public void setClient(Client client) {
		this.client = client;
	}

}
