package com.jcloisterzone.ui.grid.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class FerriesLayer extends AbstractGridLayer {

    private static final Color FERRY_COLOR = new Color(253, 241, 215);
    private Set<FeaturePointer> ferries = HashSet.empty();

    public FerriesLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        ferries = ev.getCurrentState().getCapabilityModel(FerriesCapability.class).getFerries();
    }

    @Override
    public void paint(Graphics2D g2) {

        AffineTransform orig = g2.getTransform();
        int w = getTileWidth();
        int h = getTileHeight();
        g2.setColor(FERRY_COLOR);
        g2.setStroke(new BasicStroke(h / 20));
        for (FeaturePointer ferry : ferries) {
            AffineTransform t = new AffineTransform(orig);
            Location loc = ferry.getLocation();
            Rotation rot = Location.WE.getRotationOf(loc);
            if (rot != null) {
                t.concatenate(getAffineTransform(ferry.getPosition()));
                t.concatenate(rot.inverse().getAffineTransform(w, h));
                t.concatenate(AffineTransform.getTranslateInstance(0.25*h, 0.5*h));
                g2.setTransform(t);

                for (double x = 0.0; x < 0.5; x += 0.08) {
                    g2.drawLine((int)(w*x), (int)(-0.04*h), (int)(w*x), (int)(0.04*h));
                }
            } else {
                rot = Location.NW.getRotationOf(loc);
                assert rot != null;

                t.concatenate(getAffineTransform(ferry.getPosition()));
                t.concatenate(rot.inverse().getAffineTransform(w, h));
                t.concatenate(AffineTransform.getTranslateInstance(0.25*w, 0.5*h));
                t.concatenate(AffineTransform.getRotateInstance(Math.PI * -0.25));
                g2.setTransform(t);

                for (double x = 0.0; x < 0.35; x += 0.08) {
                    g2.drawLine((int)(w*x), (int)(-0.04*h), (int)(w*x), (int)(0.04*h));
                }
            }
        }
        g2.setTransform(orig);
    }

}
