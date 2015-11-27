package com.jcloisterzone.ui.component;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


public class StrechIconPanel extends JPanel {

    private final Image image;

    public StrechIconPanel(Image image) {
        this.image = image;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
