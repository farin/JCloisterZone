package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;

import com.google.common.collect.Lists;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.grid.GridPanel;

public class TileLayer extends AbstractGridLayer {

    //keep own copy of tiles in Swing thread to prevent concurent modification ex. of tile list on game
    private List<Tile> placedTiles = Lists.newArrayList();


    public TileLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    @Override
    public void paint(Graphics2D g2) {
        for(Tile tile : placedTiles) {
            Image img = getClient().getResourceManager().getTileImage(tile.getId());
            g2.drawImage(img, getAffineTransform(img.getWidth(null), tile.getPosition(), tile.getRotation()), null);
        }
    }

    @Override
    public int getZIndex() {
        return 1;
    }

    public void tilePlaced(Tile tile) {
        placedTiles.add(tile);
    }



}
