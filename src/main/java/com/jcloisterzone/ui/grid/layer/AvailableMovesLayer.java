package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;

public class AvailableMovesLayer extends AbstractGridLayer implements GridMouseListener {

	private static final Composite ALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
	private static final Composite DISALLOWED_PREVIEW = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);
	
	private TilePlacementAction action;
	private ImmutableSet<Position> availablePositions;

	private Image previewIcon;
	private Position previewPosition;

	private Rotation realRotation;
	private Rotation previewRotation;
	private boolean allowedRotation, isAbbey;

	public AvailableMovesLayer(GridPanel gridPanel, TilePlacementAction action) {
		super(gridPanel);				
		this.action = action;
		availablePositions = ImmutableSet.copyOf(action.getAvailablePlacements().keySet());
		String tileId = action.getTile().getId();
		isAbbey = Tile.ABBEY_TILE_ID.equals(tileId);
		previewIcon = getClient().getTileTheme().getTileImage(tileId);
	}

	@Override
	public int getZIndex() {
		return 2;
	}


	@Override
	public void paint(Graphics2D g2) {
		if (previewPosition != null) {
			if (realRotation != action.getTileRotation()) {
				preparePreviewRotation(previewPosition);
			}
			Composite compositeBackup = g2.getComposite();
			g2.setComposite(allowedRotation ? ALLOWED_PREVIEW : DISALLOWED_PREVIEW);
			g2.drawImage(previewIcon, getAffineTransform(previewIcon.getWidth(null), previewPosition, previewRotation), null);
			g2.setComposite(compositeBackup);
		}
		g2.setColor(gridPanel.getClient().isClientActive() ? Color.BLACK : Color.GRAY);
		int s = getSquareSize() - 1;
		for (Position p : availablePositions) {
			g2.drawRect(getOffsetX(p),getOffsetY(p),s,s);
		}
	}

	private void preparePreviewRotation(Position p) {
		realRotation = action.getTileRotation();
		previewRotation = realRotation;
		if (isAbbey) {
			allowedRotation = true;
			return;
		}
		Set<Rotation> allowedRotations = action.getAvailablePlacements().get(p);
		if (allowedRotations.contains(previewRotation)) {
			allowedRotation = true;
		} else {
			if (allowedRotations.size() == 1) {
				previewRotation = allowedRotations.iterator().next();
				allowedRotation = true;
			} else if (action.getTile().getSymmetry() == TileSymmetry.S2) {
				previewRotation = realRotation.next();
				allowedRotation = true;
			} else {
				allowedRotation = false;
			}
		}
	}


	@Override
	public void squareEntered(MouseEvent e, Position p) {
		if (availablePositions.contains(p)) {
			previewPosition = p;
			preparePreviewRotation(p);
			gridPanel.repaint();
		}
	}

	@Override
	public void squareExited(MouseEvent e, Position p) {
		previewPosition = null;
		previewRotation  = null;
		realRotation = null;
		gridPanel.repaint();
	}


	@Override
	public void mouseClicked(MouseEvent e, Position p) {
		if (!getClient().isClientActive()) return;
		switch (e.getButton()) {
			case MouseEvent.BUTTON1 :
				assert p.equals(previewPosition) : "Expected " + previewIcon + ", get " + p;
				if (allowedRotation) {
					action.perform(getClient().getServer(), previewRotation, p);
				}
				break;
			case MouseEvent.BUTTON3 :
				action.switchAction();
				break;
		}
	}



}
