package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.RecentPlacement;
import com.jcloisterzone.ui.grid.layer.AbstractAreaLayer;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.PlacementHistory;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.theme.TileTheme;

public class GridPanel extends JComponent {

	public static int INITIAL_SQUARE_SIZE = 120;
	private static final int STARTING_GRID_SIZE = 3;

	final Client client;

	/** current board size */
	private int left, right, top, bottom;
	private int squareSize;


	private List<GridLayer> layers = Collections.synchronizedList(new LinkedList<GridLayer>());

	public GridPanel(Client client, Snapshot snapshot) {
		setDoubleBuffered(true);

		this.client = client;

		squareSize = INITIAL_SQUARE_SIZE;
		left = 0 - STARTING_GRID_SIZE / 2;
		right = 0 + STARTING_GRID_SIZE / 2;
		top = 0 - STARTING_GRID_SIZE / 2;
		bottom = 0 + STARTING_GRID_SIZE / 2;

		if (snapshot != null) {
			NodeList nl = snapshot.getTileElements();
			for(int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				Position pos = XmlUtils.extractPosition(el);
				if (pos.x <= left) left = pos.x - 1;
				if (pos.x >= right) right = pos.x + 1;
				if (pos.y <= top) top = pos.y - 1;
				if (pos.y >= bottom) bottom = pos.y + 1;
			}
		}

		calculateSize();
	}

	private void calculateSize() {
		Dimension d = new Dimension((right-left+1)*squareSize, (bottom-top+1)*squareSize);
		//setMinimumSize(d);
		//setMaximumSize(d);
		//setSize(d);
		setPreferredSize(d);
	}

	public Tile getTile(Position p) {
		return client.getGame().getBoard().get(p);
	}

	public TileTheme getTileTheme() {
		return client.getTileTheme();
	}

	public Client getClient() {
		return client;
	}

	public AnimationService getAnimationService() {
		return client.getMainPanel().getAnimationService();
	}

	public int getSquareSize() {
		return squareSize;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public void zoom(int steps) {
		int size = (int) (squareSize * Math.pow(1.3, steps));
		if (size < 25) size = 25;
		if (size > 300) size = 300;
		setZoomSize(size);
	}

	private void setZoomSize(int size) {
		if (size == squareSize) return;
		squareSize = size;

		synchronized (layers) {
			for(GridLayer layer : layers) {
				layer.zoomChanged(squareSize);
			}
		}
		calculateSize();
		revalidate();
		//TODO copy from ex-grid panel
//		if (gridPane == null) return;
//		squareSizeUpdate += sizeChange;
//		SwingUtilities.invokeLater(new ZoomUpdate());
	}

	public void showRecentHistory() {
		Collection<Tile> tiles = client.getGame().getBoard().getAllTiles();
		addLayer(new PlacementHistory(this, tiles));
	}

	public void addLayer(GridLayer layer) {
		synchronized (layers) {
			ListIterator<GridLayer> iter = layers.listIterator();
			while(iter.hasNext()) {
				GridLayer sl = iter.next();
				if (GridLayer.Z_INDEX_COMPARATOR.compare(layer, sl) <= 0) {
					iter.previous();
					break;
				}
			}
			iter.add(layer);
		}
		layer.layerAdded();
		repaint();
	}

	public void removeLayer(GridLayer layer) {
		layers.remove(layer);
		layer.layerRemoved();
		repaint();
	}

	public void removeLayer(Class<? extends GridLayer> type) {
		Iterator<GridLayer> iter = layers.iterator();
		while(iter.hasNext()) {
			GridLayer layer = iter.next();
			if (type.isInstance(layer)) {
				iter.remove();
				layer.layerRemoved();
			}
		}
		repaint();		
	}

	//TODO ok
	@SuppressWarnings("unchecked")
	synchronized
	public <T extends GridLayer> T findDecoration(Class<T> type) {
		synchronized (layers) {
			for(GridLayer layer : layers) {
				if (type.isInstance(layer)) {
					return (T) layer;
				}
			}
		}
		return null;
	}

	public void clearActionDecorations() {
		removeLayer(AbstractAreaLayer.class);
		removeLayer(TileActionLayer.class);
	}

	// delegated UI methods

	public void tilePlaced(Tile tile, TileLayer tileLayer) {
		Position p = tile.getPosition();

		removeLayer(AbstractTilePlacementLayer.class);
		removeLayer(PlacementHistory.class);

		boolean dirty = false;
		if (p.x == left) { --left; dirty = true; }
		if (p.x == right) { ++right; dirty = true; }
		if (p.y == top) { --top; dirty = true; }
		if (p.y == bottom) { ++bottom; dirty = true; }
//		if (dirty) {
//			synchronized (layers) {
//				for(GridLayer layer : layers) {
//					layer.gridChanged(left, right, top, bottom);
//				}
//			}
//		}

		tileLayer.tilePlaced(tile);

		if (client.getSettings().isShowHistory()) {
			showRecentHistory();
		}		
		boolean initialPlacement = client.getActivePlayer() == null;//if active player is null we are placing initial tiles
		if ((! initialPlacement && ! client.isClientActive()) ||
			(initialPlacement && client.getGame().getTilePack().getCurrentTile().equals(tile))) { 
			getAnimationService().registerAnimation(tile.getPosition(), new RecentPlacement(tile.getPosition()));
		}

		if (dirty) {
			calculateSize();
			revalidate();
		} else {
			repaint();
		}

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(UIManager.getColor("Panel.background"));
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(Color.LIGHT_GRAY);
		for(int i = 0; i < right-left+1; i++) {
			g2.drawLine(i*squareSize, 0, i*squareSize, (bottom-top+1)*squareSize);
			g2.drawLine((i+1)*squareSize-1, 0, (i+1)*squareSize-1, (bottom-top+1)*squareSize);
		}
		for(int i = 0; i < bottom-top+1; i++) {
			g2.drawLine(0, i*squareSize, (right-left+1)*squareSize, i*squareSize);
			g2.drawLine(0, (i+1)*squareSize-1, (right-left+1)*squareSize, (i+1)*squareSize-1);
		}
		//paint layers
		synchronized (layers) {
			for(GridLayer layer : layers) {
				layer.paint(g2);
			}
		}
	}


}
