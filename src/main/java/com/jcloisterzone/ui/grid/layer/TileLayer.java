package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.grid.GridPanel;

public class TileLayer extends AbstractGridLayer {

    //keep own copy of tiles in Swing thread to prevent concurent modification ex. of tile list on game
    private List<Tile> placedTiles = new ArrayList<>();


    public TileLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    @Override
    public void paint(Graphics2D g2) {
        //TODO nice shadow
        if (!getClient().getGridPanel().containsDecoration(AbstractTilePlacementLayer.class)) {
            g2.setColor(Color.WHITE);
            int squareSize = getSquareSize(),
                thickness = squareSize / 11;
            for (Tile tile : placedTiles) {
                Position p = tile.getPosition();
                int x = getOffsetX(p), y = getOffsetY(p);
                g2.fillRect(x-thickness, y-thickness, squareSize+2*thickness, squareSize+2*thickness);
            }
        }

        for (Tile tile : placedTiles) {
            Image img = getClient().getResourceManager().getTileImage(tile);
            g2.drawImage(img, getAffineTransform(img.getWidth(null), tile.getPosition(), tile.getRotation()), null);
        }
    }

    @Override
    public int getZIndex() {
        return 2;
    }

    public void tilePlaced(Tile tile) {
        placedTiles.add(tile);
    }



}
