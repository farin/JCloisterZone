package com.jcloisterzone.ui.grid.layer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.grid.GridPanel;

public class AbbeyPlacementLayer extends AbstractTilePlacementLayer {
	
	private AbbeyPlacementAction action;	

	public AbbeyPlacementLayer(GridPanel gridPanel, AbbeyPlacementAction action) {
		super(gridPanel, action.getSites());
	}
	
	@Override
	protected Image createPreviewIcon() {
		return getClient().getTileTheme().getTileImage(Tile.ABBEY_TILE_ID);
	}
	
	@Override	
	protected void drawPreviewIcon(Graphics2D g2, Image previewIcon, Position previewPosition) {
		Composite compositeBackup = g2.getComposite();
		g2.setComposite(ALLOWED_PREVIEW);
		g2.drawImage(previewIcon, getAffineTransform(previewIcon.getWidth(null), previewPosition), null);
		g2.setComposite(compositeBackup);
	}
	
	@Override
	public void mouseClicked(MouseEvent e, Position p) {
		if (!getClient().isClientActive()) return;
		switch (e.getButton()) {
			case MouseEvent.BUTTON1 :
				assert p.equals(getPreviewPosition()) : "Expected " + getPreviewPosition() + ", get " + p;								
				action.perform(getClient().getServer(), p);				
				break;
			case MouseEvent.BUTTON3 :
				action.switchAction();
				break;
		}
	}

}
