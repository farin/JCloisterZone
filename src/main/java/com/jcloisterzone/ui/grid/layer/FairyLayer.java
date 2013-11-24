package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.GridPanel;


public class FairyLayer extends AbstractTileLayer {

	protected static final String FAIRY_IMAGE_NAME = "fairy";

	private Image fairyImage;

	public FairyLayer(GridPanel gridPanel, Position position) {
		super(gridPanel, position);
		fairyImage = getClient().getFigureTheme().getNeutralImage(FAIRY_IMAGE_NAME);
	}


	public void paint(Graphics2D g2) {
		if (getPosition() != null) {
			g2.drawImage(fairyImage, getOffsetX(), getOffsetY(), getSquareSize(), getSquareSize(), null);
		}
	}

	@Override
	public int getZIndex() {
		return 90;
	}




}
