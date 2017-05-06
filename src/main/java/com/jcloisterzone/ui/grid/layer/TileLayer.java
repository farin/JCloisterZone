package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.TileImage;

public class TileLayer extends AbstractGridLayer {

    //keep own copy of tiles in Swing thread to prevent concurrent modification ex. of tile list on game
    private SortedSet<Tile> placedTiles = new TreeSet<>(new Comparator<Tile>() {
        @Override
        public int compare(Tile o1, Tile o2) {
        	if (o1.getPosition() == null) {
        		return o2.getPosition() == null ? 0 : 1;
        	}
            return o1.getPosition().compareTo(o2.getPosition());

        }
    });

    public TileLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);

        gc.register(this);
    }

    @Override
    public void paint(Graphics2D g2) {
        //TODO nice shadow
        if (!getClient().getGridPanel().isLayerVisible(AbstractTilePlacementLayer.class)) {

            g2.setColor(getClient().getTheme().getTileBorder());
            int xSize = getTileWidth(),
                ySize = getTileHeight(),
                thickness = xSize / 11;
            for (Tile tile : placedTiles) {
                Position p = tile.getPosition();
                if (tile.getPosition() != null) { //threading, tile can be removed
                    int x = getOffsetX(p), y = getOffsetY(p);
                    g2.fillRect(x-thickness, y-thickness, xSize+2*thickness, ySize+2*thickness);
                }
            }
        }

        for (Tile tile : placedTiles) {
            if (tile.getPosition() != null) {
                TileImage tileImg = rm.getTileImage(tile);
                g2.drawImage(tileImg.getImage(), getAffineTransform(tileImg, tile.getPosition()), null);
            }
        }
    }

    @Subscribe
    public void handleTileEvent(TileEvent ev) {
    if (ev.getType() == TileEvent.PLACEMENT) {
        tilePlaced(ev.getTile());
    } else if (ev.getType() == TileEvent.REMOVE) {
        tileRemoved(ev.getTile());
    }
    }

    private void tilePlaced(Tile tile) {
        placedTiles.add(tile);
    }

    private void tileRemoved(Tile tile) {
        placedTiles.remove(tile);
    }
}
