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

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.BridgeEvent;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

public class BridgeLayer extends AbstractGridLayer {

    private static final AlphaComposite BRIDGE_FILL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);

    private Map<Position, Location> bridges = new HashMap<>();

    private MeepleLayer meepleLayer;

    public BridgeLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);

        gc.register(this);
    }

    @Override
    public void paint(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();
        for (Entry<Position, Location> entry : bridges.entrySet()) {
            Position pos = entry.getKey();
            Location loc = entry.getValue();
            Tile tile = getGame().getBoard().get(pos);
            Area a = rm.getBridgeArea(tile, getSquareSize(), loc).getTrackingArea();
            a.transform(AffineTransform.getTranslateInstance(getOffsetX(pos), getOffsetY(pos)));

            g2.setColor(Color.BLACK);
            g2.setComposite(BRIDGE_FILL_COMPOSITE);
            g2.fill(a);

        }
        g2.setComposite(oldComposite);

        meepleLayer.paintMeeplesOnBridges(g2);
    }

    @Subscribe
    public void onBridgeEvent(BridgeEvent ev) {
	gridPanel.clearActionDecorations();

        if (ev.getType() == BridgeEvent.DEPLOY) {
            bridgeDeployed(ev.getPosition(), ev.getLocation());
        } else if (ev.getType() == BridgeEvent.REMOVE) {
            bridgeRemoved(ev.getPosition());
        }
    }

    private void bridgeDeployed(Position pos, Location loc) {
        bridges.put(pos, loc);
    }

    private void bridgeRemoved(Position pos) {
        bridges.remove(pos);
    }

    public MeepleLayer getMeepleLayer() {
        return meepleLayer;
    }

    public void setMeepleLayer(MeepleLayer meepleLayer) {
        this.meepleLayer = meepleLayer;
    }
}
