package com.jcloisterzone.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class JLabelWithAntialiasing extends JLabel { //TODO rename JLabelAntialised

	public JLabelWithAntialiasing() {
		super();
	}

	public JLabelWithAntialiasing(String text) {
		super(text);
	}

	public JLabelWithAntialiasing(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	@Override
	public void paint(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
	}

}
