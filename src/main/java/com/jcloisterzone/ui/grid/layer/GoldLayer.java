package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.GoldChangeEvent;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

public class GoldLayer extends AbstractGridLayer {

    private final Image goldImage;
    private final double widthHeightRatio;
    private final Map<Position, Integer> placedGold = new HashMap<>();

    private final static Color FILL_COLOR = new Color(40,40,40,120);

    public GoldLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
		goldImage = rm.getImage("neutral/gold");
        widthHeightRatio = goldImage.getWidth(null) / (double) goldImage.getHeight(null);

        gc.register(this);
    }

    @Subscribe
    public void onGoldChangeEvent(GoldChangeEvent ev) {
        setGoldCount(ev.getPos(), ev.getCount());
        gridPanel.repaint();
    }

    private void setGoldCount(Position pos, int count) {
        if (count == 0) {
            placedGold.remove(pos);
        } else {
            placedGold.put(pos, count);
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        int size = getTileWidth();
        int w = (int)(size*0.4);
        int h = (int)(w / widthHeightRatio);
        g2.setColor(FILL_COLOR);
        for (Entry<Position, Integer> entry : placedGold.entrySet()) {
            Position pos = entry.getKey();
            int tx = (int)(size*0.45);

            AffineTransform at = getAffineTransformIgnoringRotation(pos);
            Rectangle rect = new Rectangle(tx, 0, w + (int)(size*0.15), h);
            g2.fill(at.createTransformedShape(rect));

            drawImageIgnoringRotation(g2, goldImage, pos, tx, 0, w, h);
            ImmutablePoint point = new ImmutablePoint(90,10).rotate100(gridPanel.getBoardRotation().inverse());
            drawAntialiasedTextCentered(g2, ""+entry.getValue(), 20, entry.getKey(), point, Color.WHITE, null);
        }
    }
}
