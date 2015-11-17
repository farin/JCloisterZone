package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

public class TileLayer extends AbstractGridLayer {

    //keep own copy of tiles in Swing thread to prevent concurent modification ex. of tile list on game
    private List<Tile> placedTiles = new ArrayList<>();

    public TileLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);

        gc.register(this);
    }

    @Override
    public void paint(Graphics2D g2) {
        //TODO nice shadow
        if (!getClient().getGridPanel().isLayerVisible(AbstractTilePlacementLayer.class)) {
            g2.setColor(Color.WHITE);
            int squareSize = getSquareSize(),
                thickness = squareSize / 11;
            for (Tile tile : placedTiles) {
                Position p = tile.getPosition();
                if (tile.getPosition() != null) { //threading, tile can be removed
                    int x = getOffsetX(p), y = getOffsetY(p);
                    g2.fillRect(x-thickness, y-thickness, squareSize+2*thickness, squareSize+2*thickness);
                }
            }
        }

        for (Tile tile : placedTiles) {
            if (tile.getPosition() != null) {
                Image img = rm.getTileImage(tile);
                g2.drawImage(img, getAffineTransform(img.getWidth(null), tile.getPosition(), tile.getRotation()), null);
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
