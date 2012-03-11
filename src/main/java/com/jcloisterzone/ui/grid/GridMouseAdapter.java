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
		int cx = e.getX() - gridPanel.getOffsetX();
		int cy = e.getY() - gridPanel.getOffsetY();
		int x = cx / sqSize + ((cx < 0) ? -1 : 0);
		int y = cy / sqSize + ((cy < 0) ? -1 : 0);	
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
