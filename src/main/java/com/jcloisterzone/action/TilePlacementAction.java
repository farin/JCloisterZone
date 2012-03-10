package com.jcloisterzone.action;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;

public class TilePlacementAction extends PlayerAction {
	
	private final Tile tile;
	private final Map<Position, Set<Rotation>> placements;
	
	private Rotation tileRotation = Rotation.R0;
			
	public TilePlacementAction(Tile tile, Map<Position, Set<Rotation>> placements) {			
		this.tile = tile;
		this.placements = placements;
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public Rotation getTileRotation() {
		return tileRotation;
	}
	
	public Map<Position, Set<Rotation>> getAvailablePlacements() {
		return placements;
	}
	
	@Override
	public Image getImage(Player player, boolean active) {
		Image img =  client.getTileTheme().getTileImage(tile.getId());
		if (tileRotation != Rotation.R0) {
			//TODO without buffered image
			int w = img.getWidth(null), h = img.getHeight(null);
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);			
			AffineTransform at = tileRotation.getAffineTransform(w);
			Graphics2D ig = bi.createGraphics();
			ig.drawImage(img, at, null);
			return bi;
		}
		return img;
	}

	public void perform(Client2ClientIF server, Rotation rotation, Position p) {
		server.placeTile(rotation, p);
	}
	
	@Override
	protected GridLayer createGridLayer() {
		return new TilePlacementLayer(client.getGridPanel(), this);		
	}
	
	@Override
	public void switchAction() {
		tileRotation = tileRotation.next();	
		client.getMainPanel().repaint();		
	}

}
