package com.jcloisterzone.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class JLabelAntialised extends JLabel {

	private static final long serialVersionUID = -619469286371707527L;

	public JLabelAntialised() {
		super();
	}

	public JLabelAntialised(String text) {
		super(text);
	}

	public JLabelAntialised(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	@Override
	public void paint(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paint(g);
	}

}
