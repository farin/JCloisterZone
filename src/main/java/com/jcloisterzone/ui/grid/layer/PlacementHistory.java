package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

public class PlacementHistory extends AbstractGridLayer {

    private static final Color COLOR = new Color(0,0,0,128);
    private static final ImmutablePoint POINT = new ImmutablePoint(50,50);

    private Map<Position, String> history = new HashMap<>();

    public PlacementHistory(GridPanel gridPanel, Collection<Tile> placedTiles) {
        super(gridPanel);
        int i = placedTiles.size();
        int limit = getClient().getGame().getAllPlayers().length;
        for (Tile t : placedTiles) {
            if (i <= limit) {
                history.put(t.getPosition(), "" + i);
            }
            i--;
        }
    }


    @Override
    public void paint(Graphics2D g) {
        for (Entry<Position, String> entry : history.entrySet()) {
            drawAntialiasedTextCentered(g, entry.getValue(), 80, entry.getKey(), POINT, COLOR, null);
        }

    }

    @Override
    public int getZIndex() {
        return 1001;
    }

}
