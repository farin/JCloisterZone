package com.jcloisterzone.ui.controls;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.JLabel;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.Client;


public class NextSquare extends JLabel {

	public static final int SIZE = 120;

	final Client client;
	private Tile tile;
	private Image tileImage;
	private Image actionImage;

	public NextSquare(Client client) {
		this.client = client;
		setPreferredSize(new Dimension(SIZE, SIZE));
	}

	public void setSelectedFigure(Image img) {
		this.tile = null;
		this.tileImage = null;
		this.actionImage = img;
		repaint();
	}

	public void setTile(Tile tile) {
		this.tileImage = client.getTileTheme().getTileImage(tile.getId());
		this.tile = tile;
		this.actionImage = null;
		repaint();
	}

	public Tile getTile() {
		return tile;
	}

	public void setEmptyIcon() {
		this.tile = null;
		this.tileImage =  client.getTileTheme().getEmptyImage();
		this.actionImage = null;
		repaint();
	}


	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		if (tile != null) {
			AffineTransform t = tile.getRotation().getAffineTransform(SIZE);
			double ratio =  SIZE / (double) tileImage.getWidth(null);
			t.concatenate(AffineTransform.getScaleInstance(ratio, ratio));
			g2.drawImage(tileImage, t, null);
		} else if (tileImage != null) {
			//empty icon
			//g2.drawImage(tileImage, 0, 0, SIZE, SIZE, 0, 0, tileImage.getWidth(null), tileImage.getHeight(null), null);
			g2.drawImage(tileImage, 0, 0, SIZE, SIZE, null);
		} else if (actionImage != null){
			int offset = (int) (SIZE * 0.125);
			g2.drawImage(actionImage, offset, offset, SIZE-2*offset, SIZE-2*offset, null);
		}
	}


}

