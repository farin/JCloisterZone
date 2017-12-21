package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;

import io.vavr.collection.List;
import io.vavr.collection.Set;

public class BridgeLayer extends AbstractGridLayer {

    private static final AlphaComposite BRIDGE_FILL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);

    /** bridges */
    private List<FeatureArea> model = List.empty();

    private MeepleLayer meepleLayer;

    public BridgeLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        boolean bridgesChanged = false;
        for (PlayEvent pe : ev.getPlayEventsSymmetricDifference()) {
            if (pe instanceof TokenPlacedEvent && ((TokenPlacedEvent)pe).getToken() == Token.BRIDGE) {
                bridgesChanged = true;
                break;
            }
        }

        if (bridgesChanged) {
            model = createModel(ev.getCurrentState());
        }
    }

    private List<FeatureArea> createModel(GameState state) {
        Set<FeaturePointer> placedBridges = state.getCapabilityModel(BridgeCapability.class);
        return placedBridges.toList().map(bridge -> {
            Position pos = bridge.getPosition();
            Location loc = bridge.getLocation();
            return rm.getBridgeArea(loc).translateTo(pos);
        });
    }

    @Override
    public void paint(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();
        g2.setColor(Color.BLACK);
        g2.setComposite(BRIDGE_FILL_COMPOSITE);

        AffineTransform scaleTx = getZoomScale();
        for (FeatureArea bridgeArea : model) {
            Area a = bridgeArea.getDisplayArea().createTransformedArea(scaleTx);
            g2.fill(a);
        }
        g2.setComposite(oldComposite);

        meepleLayer.paintMeeplesOnBridges(g2);
    }


    public MeepleLayer getMeepleLayer() {
        return meepleLayer;
    }

    public void setMeepleLayer(MeepleLayer meepleLayer) {
        this.meepleLayer = meepleLayer;
    }
}
