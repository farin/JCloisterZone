package com.jcloisterzone.ui.gtk;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Fix for GTK+ LaF to respect UIManager defaults
 */
public class ThemedJPanel extends JPanel {


	public ThemedJPanel() {
		super();
	}

	public ThemedJPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public ThemedJPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public ThemedJPanel(LayoutManager layout) {
		super(layout);
	}

	{
		setBackground(UIManager.getColor("Panel.background"));
		setForeground(UIManager.getColor("Panel.foreground"));
	}

}
