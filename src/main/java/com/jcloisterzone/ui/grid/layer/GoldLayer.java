package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

// TODO nice to have, merge with GoldCapability ?
public class GoldLayer extends AbstractGridLayer {

    private final static Color FILL_COLOR = new Color(40,40,40,120);

    private final Image goldImage;
    private final double widthHeightRatio;

    private Map<Position, Integer> placedGold = HashMap.empty();

    public GoldLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        goldImage = rm.getImage("neutral/gold");
        widthHeightRatio = goldImage.getWidth(null) / (double) goldImage.getHeight(null);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        placedGold = ev.getCurrentState().getCapabilityModel(GoldminesCapability.class);
    }


    @Override
    public void paint(Graphics2D g2) {
        int size = getTileWidth();
        int w = (int)(size*0.4);
        int h = (int)(w / widthHeightRatio);
        g2.setColor(FILL_COLOR);
        placedGold.forEach((pos, count) -> {
            int tx = (int)(size*0.45);

            AffineTransform at = getAffineTransformIgnoringRotation(pos);
            Rectangle rect = new Rectangle(tx, 0, w + (int)(size*0.15), h);
            g2.fill(at.createTransformedShape(rect));

            drawImageIgnoringRotation(g2, goldImage, pos, tx, 0, w, h);
            ImmutablePoint point = new ImmutablePoint(90,10).rotate100(gridPanel.getBoardRotation().inverse());
            drawAntialiasedTextCentered(g2, ""+count, 20, pos, point, Color.WHITE, null);
        });
    }
}
