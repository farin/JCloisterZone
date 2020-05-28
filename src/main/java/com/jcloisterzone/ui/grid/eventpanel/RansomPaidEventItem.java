package com.jcloisterzone.ui.grid.eventpanel;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.RansomPaidEvent;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.ui.grid.GameEventsPanel;
import com.jcloisterzone.ui.theme.Theme;

import java.awt.*;

public class RansomPaidEventItem extends EventItem {

    private static Font FONT_SCORE = new Font("Georgia", Font.PLAIN, 12);
    private static final BasicStroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final Theme theme;

    public RansomPaidEventItem(Theme theme, PlayEvent event, Color turnColor, Color color) {
        super(event, turnColor, color);
        this.theme = theme;
    }

    @Override
    public RansomPaidEvent getEvent() {
        return (RansomPaidEvent) super.getEvent();
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setFont(FONT_SCORE);
        RansomPaidEvent ev = getEvent();
        drawTextShadow(g2, "-3", 2, 10, getColor());
        Color jailerColor = ev.getJailer().getColors().getFontColor();
        drawTextShadow(g2, "+3", 12, 25, jailerColor);

        int padding = 6;
        int x1 = padding + 2;
        int x2 = GameEventsPanel.ICON_WIDTH  - padding - 2;
        int y1 = padding + 2;
        int y2 = GameEventsPanel.ICON_HEIGHT  - padding - 2;

        g2.setStroke(STROKE);
        g2.setColor(Color.GRAY);
        g2.drawLine(x2, y1, x1, y2);
    }

    private void drawTextShadow(Graphics2D g2, String text, int x, int y, Color color) {
        Color shadowColor = theme.getFontShadowColor();
        if (shadowColor != null) {
            g2.setColor(shadowColor);
            g2.drawString(text, x+1, y+1);
        }
        g2.setColor(color);
        g2.drawString(text, x, y);
    }
}
