package com.jcloisterzone.ui.grid.eventpanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.ui.grid.GameEventsPanel;

public class ImageEventItem extends EventItem {

    protected Image image;
    protected int padding;

    public ImageEventItem(PlayEvent event, Color turnColor, Color color) {
        super(event, turnColor, color);
    }

    @Override
    public void draw(Graphics2D g2) {
        int size = GameEventsPanel.ICON_WIDTH  - 2 * padding;
        g2.drawImage(image, padding, padding, size, size, null);
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




}
