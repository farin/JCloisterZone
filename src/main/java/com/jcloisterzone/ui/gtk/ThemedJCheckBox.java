package com.jcloisterzone.ui.gtk;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;

/**
 * Fix for GTK+ LaF to respect UIManager defaults
 */
public class ThemedJCheckBox extends JCheckBox {

	public ThemedJCheckBox() {
		super();
	}

	public ThemedJCheckBox(Action a) {
		super(a);
	}

	public ThemedJCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
	}

	public ThemedJCheckBox(Icon icon) {
		super(icon);
	}

	public ThemedJCheckBox(String text, boolean selected) {
		super(text, selected);
	}

	public ThemedJCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	public ThemedJCheckBox(String text, Icon icon) {
		super(text, icon);
	}

	public ThemedJCheckBox(String text) {
		super(text);
	}

	{
		//setForeground(UIManager.getColors("CheckBox.foreground"));
		setBackground(UIManager.getColor("CheckBox.background"));
		Color c = UIManager.getColor("CheckBox.foreground");
		if (c != null) {
			//HACK for some reason, direct passing not work in GTK LaF
			setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue()));
		}

	}

}
