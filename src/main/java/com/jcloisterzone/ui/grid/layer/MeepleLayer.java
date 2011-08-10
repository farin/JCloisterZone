package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.theme.FigureTheme;

public class MeepleLayer extends AbstractGridLayer {

	protected float FIGURE_SIZE_RATIO = 0.35f;

	private Map<Meeple, PositionedImage> images = Maps.newHashMap();
	//TODO own layer ???
	private List<PositionedImage> permanentImages = Lists.newArrayList();

	public MeepleLayer(GridPanel gridPanel) {
		super(gridPanel);
	}

	@Override
	public int getZIndex() {
		return 50;
	}

	@Override
	public void paint(Graphics2D g) {
		int size = (int) (getSquareSize() * FIGURE_SIZE_RATIO); //TODO no resize - resize direct images
		for(PositionedImage mi : Iterables.concat(images.values(), permanentImages)) {
			ImmutablePoint scaled = mi.offset.scale(getSquareSize(), size);
			//TODO caching scaled instanci
			Image img = mi.image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
			img = new ImageIcon(img).getImage();
			//---
			g.drawImage(img, getOffsetX(mi.position) + scaled.getX(), getOffsetY(mi.position) + scaled.getY(), gridPanel);
		}

	}

	public void meepleDeployed(Meeple m) {
		Color c = getClient().getPlayerColor(m.getPlayer());
		FigureTheme theme = getClient().getFigureTheme();

		ImmutablePoint offset = getClient().getTileTheme().getFigurePlacement(gridPanel.getTile(m.getPosition()), m);
		Image image = theme.getFigureImage(m.getClass(), c,  getExtraDecoration(m));
		images.put(m, new PositionedImage(m.getPosition(), offset, image));
	}

	public void meepleUndeployed(Meeple m) {
		images.remove(m);
	}

	public void addPermanentImage(Position position, ImmutablePoint offset, Image image) {
		permanentImages.add(new PositionedImage(position, offset, image));
	}

	private static class PositionedImage {
		public Position position;
		public ImmutablePoint offset;
		public Image image;

		public PositionedImage(Position position, ImmutablePoint offset, Image image) {
			this.position = position;
			this.offset = offset;
			this.image = image;
		}
	}

	//TODO path from Theme
	public String getExtraDecoration(Meeple m) {
		if (m instanceof Follower && m.getFeature() instanceof Farm) {
			return "farm.png";
		}
		if (m.getFeature() instanceof Tower) {
			if (m instanceof BigFollower) {
				return "big_tower.png";
			} else {
				return "tower.png";
			}
		}
		return null;
	}


}