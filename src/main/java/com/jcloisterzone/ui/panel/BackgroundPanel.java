package com.jcloisterzone.ui.panel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {


	static private int W = 396;
	static private int H = 396;
	static private Image bg = new ImageIcon(BackgroundPanel.class.getClassLoader().getResource("sysimages/panel_bg.png")).getImage();

	public BackgroundPanel() {
		super();
	}

	public BackgroundPanel(LayoutManager layout) {
		super(layout);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = 0;
		int y = 0;
		while(y < getHeight()) {
			x = 0;
			while(x < getWidth()) {
				g.drawImage(bg, x, y, W, H, this);
				x += W;
			}
			y += H;
		}
	}

}
