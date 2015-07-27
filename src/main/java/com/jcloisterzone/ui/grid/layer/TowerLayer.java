package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;


public class TowerLayer extends AbstractGridLayer {

    private final static Color FILL_COLOR = new Color(40,40,40,150);

    private Map<Position, Integer> heights = new HashMap<>();

    public TowerLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void paint(Graphics2D g2) {
        g2.setColor(FILL_COLOR);
        for (Entry<Position, Integer> entry : heights.entrySet()) {
            Tile tile = gridPanel.getTile(entry.getKey());
            Area ra = getClient().getResourceManager().getMeepleTileArea(tile, getSquareSize(), Location.TOWER).getTrackingArea();
            g2.fill(transformArea(ra, entry.getKey()));
            drawAntialiasedTextCenteredNoScale(g2,"" + entry.getValue(), 22, entry.getKey(),
                    new ImmutablePoint((int)ra.getBounds2D().getCenterX(), (int)ra.getBounds2D().getCenterY()), Color.WHITE, null);
        }
    }

    public void setTowerHeight(Position p, int towerHeight) {
        heights.put(p, towerHeight);
    }

}
