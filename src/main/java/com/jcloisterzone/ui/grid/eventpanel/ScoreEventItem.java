package com.jcloisterzone.ui.grid.eventpanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.ui.grid.GameEventsPanel;
import com.jcloisterzone.ui.theme.Theme;

public class ScoreEventItem extends EventItem {

    private static Font FONT_SCORE = new Font("Georgia", Font.PLAIN, 24);

    private final Theme theme;

    public ScoreEventItem(Theme theme, PlayEvent event, Color turnColor, Color color) {
        super(event, turnColor, color);
        this.theme = theme;
    }

    @Override
    public ScoreEvent getEvent() {
        return (ScoreEvent) super.getEvent();
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setFont(FONT_SCORE);
        ScoreEvent ev = getEvent();
        Color color = ev.getReceiver().getColors().getFontColor();
        int offset = ev.getPoints() > 9 ? 0 : 8;
        drawTextShadow(g2, "" + ev.getPoints(), offset, GameEventsPanel.ICON_HEIGHT - 8, color);

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
