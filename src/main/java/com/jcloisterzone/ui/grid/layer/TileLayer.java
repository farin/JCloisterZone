package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.util.Comparator;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.TileImage;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;

public class TileLayer extends AbstractGridLayer {

    private TilePlacementLayer tilePlacementLayer;

    class OrderByRowsComparator implements Comparator<Tuple2<Position, PlacedTile>> {
        @Override
        public int compare(Tuple2<Position, PlacedTile> o1, Tuple2<Position, PlacedTile> o2) {
            if (o1._1 == null) {
                return o2._1 == null ? 0 : 1;
            }
            return o1._1.compareTo(o2._1);
        }
    }

    private SortedSet<Tuple2<Position, PlacedTile>> sortedPlacedTiles = TreeSet.empty();


    public TileLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        if (ev.hasPlacedTilesChanged()) {
            LinkedHashMap<Position, PlacedTile> placedTiles = ev.getCurrentState().getPlacedTiles();
            sortedPlacedTiles = placedTiles.toSortedSet(new OrderByRowsComparator());
            gridPanel.repaint();
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        //TODO nice shadow
        if (!tilePlacementLayer.isVisible()) {
            g2.setColor(getClient().getTheme().getTileBorder());
            int xSize = getTileWidth(),
                ySize = getTileHeight(),
                thickness = xSize / 11;
            for (Tuple2<Position, PlacedTile> t : sortedPlacedTiles) {
                Position p = t._1;
                int x = getOffsetX(p), y = getOffsetY(p);
                g2.fillRect(x-thickness, y-thickness, xSize+2*thickness, ySize+2*thickness);
            }
        }


        for (Tuple2<Position, PlacedTile> t : sortedPlacedTiles) {
            Position p = t._1;
            Tile tdef = t._2.getTile();
            Rotation rot = t._2.getRotation();
            TileImage tileImg = rm.getTileImage(tdef.getId(), rot);
            g2.drawImage(tileImg.getImage(), getAffineTransform(tileImg, p), null);
        }

        if (tilePlacementLayer.isVisible()) {
            tilePlacementLayer.paintBridgePreview(g2);
        }
    }

    public TilePlacementLayer getTilePlacmentLayer() {
        return tilePlacementLayer;
    }

    public void setTilePlacmentLayer(TilePlacementLayer tilePlacementLayer) {
        this.tilePlacementLayer = tilePlacementLayer;
    }

}
