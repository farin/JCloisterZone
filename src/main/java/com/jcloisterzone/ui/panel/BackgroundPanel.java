package com.jcloisterzone.ui.panel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {

    static private int W = 396;
    static private int H = 396;
    static public Image DEFAULT_BACKGROUND_IMAGE = new ImageIcon(BackgroundPanel.class.getClassLoader().getResource("sysimages/panel_bg.png")).getImage();

    private Image backgroundImage = DEFAULT_BACKGROUND_IMAGE;

    public BackgroundPanel() {
        super();
    }

    public BackgroundPanel(LayoutManager layout) {
        super(layout);
    }


    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage ==  null) return;
        int x = 0;
        int y = 0;
        while(y < getHeight()) {
            x = 0;
            while(x < getWidth()) {
                g.drawImage(backgroundImage, x, y, W, H, this);
                x += W;
            }
            y += H;
        }
    }

}
