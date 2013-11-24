package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.ui.grid.GridPanel;

public class CastleLayer extends AbstractGridLayer {
	
	private static class DeployedCastle {
		Position position;
		Rotation rotation;
		
		public DeployedCastle(Position position, Rotation rotation) {
			this.position = position;
			this.rotation = rotation;
		} 				
	}
	
	private List<DeployedCastle> castles = new ArrayList<>();
	private Image castleImage;
	
	public CastleLayer(GridPanel gridPanel) {
		super(gridPanel);
		castleImage = getClient().getFigureTheme().getNeutralImage("castle");
	}
	
	public void castleDeployed(Castle castle1, Castle castle2) { 
		Position p1 = castle1.getTile().getPosition();
		Position p2 = castle2.getTile().getPosition();
		Position pos;
		Rotation rot;
		
		if (p1.x == p2.x) {
			pos = p1.y < p2.y ? p1 : p2;
			rot = Rotation.R0;
		} else {
			pos = p1.x < p2.x ? p1 : p2;
			rot = Rotation.R90;
		}
		castles.add(new DeployedCastle(pos, rot));
	}

	@Override
	public void paint(Graphics2D g2) {	
		int size = getSquareSize();
		for (DeployedCastle dc : castles) {
			if (dc.rotation == Rotation.R0) {
				g2.drawImage(castleImage, getOffsetX(dc.position), getOffsetY(dc.position) + size/2, size, size, null);
			} else {
//				AffineTransform at = Rotation.R90.getAffineTransform(size);
//				at.concatenate(AffineTransform.getTranslateInstance(getOffsetX(dc.position) + size/2, getOffsetY(dc.position)));
//				g2.drawImage(castleImage, at, null);
				
				//TODO rotated
				g2.drawImage(castleImage, getOffsetX(dc.position) + size/2, getOffsetY(dc.position), size, size, null);
			}
		}
	}
	
	@Override
	public int getZIndex() {
		return 45;
	}
		

}
