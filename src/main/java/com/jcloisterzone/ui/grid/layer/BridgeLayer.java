package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.grid.GridPanel;

public class BridgeLayer extends AbstractGridLayer {

    private static final AlphaComposite BRIDGE_FILL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);
    //private static final AlphaComposite BRIDGE_STROKE_COMPOSITE = AlphaComposite.SrcOver;

    public BridgeLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    //TODO store direct images as in Meeple layer???
    private Map<Tile, Location> bridges = new HashMap<>();

    @Override
    public void paint(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();
//		Stroke oldStroke = g2.getStroke();
//		g2.setStroke(new BasicStroke(getSquareSize() * 0.015f));
        for (Entry<Tile, Location> entry : bridges.entrySet()) {
            //devel code only - use image instead
            Tile tile = entry.getKey();
            Location loc = entry.getValue();
            Position pos = tile.getPosition();
            Area a = getClient().getResourceManager().getBridgeArea(tile, getSquareSize(), loc);
            a.transform(AffineTransform.getTranslateInstance(getOffsetX(pos), getOffsetY(pos)));
            g2.setColor(Color.BLACK);
            g2.setComposite(BRIDGE_FILL_COMPOSITE);
            g2.fill(a);
//			g2.setColor(Color.BLACK);
//			g2.setComposite(BRIDGE_STROKE_COMPOSITE);
//			g2.draw(a);

        }
//		g2.setStroke(oldStroke);
        g2.setComposite(oldComposite);

    }

    @Override
    public int getZIndex() {
        return 45;
    }

    public void bridgeDeployed(Position pos, Location loc) {
        Tile tile = getClient().getGame().getBoard().get(pos);
        bridges.put(tile, loc);
    }

}
