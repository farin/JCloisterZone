package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public abstract class AbstractAreaLayer extends AbstractGridLayer implements GridMouseListener {

	private static final AlphaComposite AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);

	private Map<Location, Area> areas;
	private Location selectedLocation;
	private Position selectedPosition;

	public AbstractAreaLayer(GridPanel gridPanel) {
		super(gridPanel);
	}

	private class MoveTrackingGridMouseAdapter extends GridMouseAdapter {

		public MoveTrackingGridMouseAdapter(GridPanel gridPanel, GridMouseListener listener) {
			super(gridPanel, listener);
		}

		@Override
		public void mouseMoved(MouseEvent e) { 
			super.mouseMoved(e);						
			if (areas == null) return;
			int size = getSquareSize();
			int x = e.getX() % size;
			int y = e.getY() % size;
			Location swap = null;
			for (Entry<Location, Area> enrty : areas.entrySet()) {
				if (enrty.getValue().contains(x, y)) {
					if (swap != null) { // 2 areas at point - select no one
						swap = null;
						break;
					}
					swap = enrty.getKey();
				}
			}			
			if (swap != selectedLocation) {
				selectedLocation = swap;
				gridPanel.repaint();				
				//RepaintManager.currentManager(gridPanel).addDirtyRegion(gridPanel, x * size, y * size, size, size);			
				//gridPanel.repaint(0, x * size, y * size, size, size);
			}
		}

	}

	@Override
	protected GridMouseAdapter createGridMouserAdapter(GridMouseListener listener) {
		return new MoveTrackingGridMouseAdapter(gridPanel, listener);
	}

	private void cleanAreas() {
		areas = null;
		selectedPosition = null;
		selectedLocation = null;
	}

	@Override
	public void zoomChanged(int squareSize) {
		Position prevSelectedPosition = selectedPosition;
		super.zoomChanged(squareSize);
		if (selectedPosition != null && selectedPosition.equals(prevSelectedPosition)) {
			//no square enter/leave trigger in this case - refresh areas
			areas = prepareAreas(gridPanel.getTile(selectedPosition), selectedPosition);
		}
	}

	@Override
	public void squareEntered(MouseEvent e, Position p) {
		Tile tile = gridPanel.getTile(p);
		if (tile != null) {
			selectedPosition = p;
			areas = prepareAreas(tile, p);
		}
	}

	protected abstract Map<Location, Area> prepareAreas(Tile tile, Position p);


	@Override
	public void squareExited(MouseEvent e, Position p) {
		if (selectedPosition != null) {
			cleanAreas();
			gridPanel.repaint();
		}
	}

	protected abstract void performAction(Position pos, Location selected);

	@Override
	public void mouseClicked(MouseEvent e, Position pos) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (selectedLocation != null) {
				performAction(pos, selectedLocation);
			}
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			getClient().getControlPanel().getActionPanel().nextAction();
		}
	}


	@Override
	public void paint(Graphics2D g2) {
		if (selectedLocation != null && areas != null) {
			g2.setColor(getClient().getPlayerColor());
			Composite old = g2.getComposite();
			g2.setComposite(AREA_ALPHA_COMPOSITE);
			g2.fill(transformArea(areas.get(selectedLocation), selectedPosition));
			g2.setComposite(old);
		}
	}

	@Override
	public int getZIndex() {
		return 100;
	}

}
