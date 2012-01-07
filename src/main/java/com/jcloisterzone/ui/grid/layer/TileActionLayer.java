package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public class TileActionLayer extends AbstractGridLayer implements GridMouseListener {

	private SelectTileAction action;

	public TileActionLayer(GridPanel gridPanel, SelectTileAction action) {
		super(gridPanel);
		this.action = action;
	}

	@Override
	public int getZIndex() {
		return 70;
	}

	public void paint(Graphics2D g2) {
		Image img;
		if (action instanceof FairyAction) {
			img = getClient().getControlsTheme().getActionDecoration("fairy");
		} else if (action instanceof TowerPieceAction) {
			img = getClient().getControlsTheme().getActionDecoration("tower");
		} else {
			throw new UnsupportedOperationException("Unknown action");
		}
		int imgSize = img.getWidth(null);
		for(Position pos : action.getSites()) {
			g2.drawImage(img, getAffineTransform(imgSize, pos), null);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e, Position p) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (action.getSites().contains(p)) {
				action.perform(getClient().getServer(), p);
			}
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			getClient().getControlPanel().getActionPanel().nextAction();
		}
	}


	@Override
	public void squareEntered(MouseEvent e, Position p) { }
	@Override
	public void squareExited(MouseEvent e, Position p) {  }



}
