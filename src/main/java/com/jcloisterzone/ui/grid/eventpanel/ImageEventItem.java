package com.jcloisterzone.ui.grid.eventpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.ui.grid.GameEventsPanel;

public class ImageEventItem extends EventItem {

    private static final BasicStroke BORDER_STROKE = new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    protected Image image;
    protected int padding;
    protected boolean drawCross;

    public ImageEventItem(PlayEvent event, Color turnColor, Color color) {
        super(event, turnColor, color);
    }

    @Override
    public void draw(Graphics2D g2) {
        int width = GameEventsPanel.ICON_WIDTH  - 2 * padding;
        int height = GameEventsPanel.ICON_HEIGHT  - 2 * padding;
        g2.drawImage(image, padding, padding, width, height, null);

        if (drawCross) {
            int x1 = padding + 2;
            int x2 = GameEventsPanel.ICON_WIDTH  - padding - 2;
            int y1 = padding + 2;
            int y2 = GameEventsPanel.ICON_HEIGHT  - padding - 2;
            g2.setStroke(BORDER_STROKE);
            g2.setColor(Color.WHITE);
            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x2, y1, x1, y2);
            g2.setStroke(STROKE);
            g2.setColor(Color.BLACK);
            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x2, y1, x1, y2);
        }
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public boolean isDrawCross() {
        return drawCross;
    }

    public void setDrawCross(boolean drawCross) {
        this.drawCross = drawCross;
    }
}
