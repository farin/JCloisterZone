package com.jcloisterzone.ui.grid;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.jcloisterzone.board.Position;

public class GridMouseAdapter implements MouseListener, MouseMotionListener {

	final GridPanel gridPanel;
	final GridMouseListener listener;

	private Position currentPosition;

	public GridMouseAdapter(GridPanel gridPanel, GridMouseListener listener) {
		this.gridPanel = gridPanel;
		this.listener = listener;
	}

	private Position getGridPosition(MouseEvent e) {
		int sqSize = gridPanel.getSquareSize();
		int x = (e.getX() / sqSize) + gridPanel.getLeft();
		int y = (e.getY() / sqSize) + gridPanel.getTop();
		return new Position(x, y);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Position p = getGridPosition(e);
		if (currentPosition != null && ! currentPosition.equals(p)) {
			listener.squareExited(e, currentPosition);
			currentPosition = null;
		}
		if (p != null && ! p.equals(currentPosition)) {
			currentPosition = p;
			listener.squareEntered(e, currentPosition);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Position p = getGridPosition(e);
		if (p != null) {
			listener.mouseClicked(e, p);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (currentPosition != null) {
			listener.squareExited(e, currentPosition);
			currentPosition = null;
		}

	}

}
