package com.jcloisterzone.ui.grid.eventpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.ui.grid.GameEventsPanel;

public class TileDiscardedEventItem extends ImageEventItem {

    private static final BasicStroke STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public TileDiscardedEventItem(PlayEvent event, Color turnColor, Color color) {
        super(event, turnColor, color);
    }

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);
        g2.setStroke(STROKE);

        int p1 = padding + 2;
        int p2 = GameEventsPanel.ICON_WIDTH  - padding - 2;
        g2.setColor(Color.BLACK);
        g2.drawLine(p1, p1, p2, p2);
        g2.drawLine(p2, p1, p2, p1);
    }

}
