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

    private final Map<Position, String> history = new HashMap<>();
    private final Collection<Tile> placedTilesView;

    public PlacementHistory(GridPanel gridPanel, Collection<Tile> placedTilesView) {
        super(gridPanel);
        this.placedTilesView = placedTilesView;
    }

    public void update() {
        history.clear();
        int i = placedTilesView.size();
        int limit = getGame().getAllPlayers().length;
        for (Tile t : placedTilesView) {
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
}
