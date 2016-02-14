package com.jcloisterzone.ui.gtk;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.UIManager;

/**
 * Fix for GTK+ LaF to respect UIManager defaults
 */
public class ThemedJList<T> extends JList<T> {

	public ThemedJList() {
		super();
	}

	public ThemedJList(ListModel<T> dataModel) {
		super(dataModel);
	}

	public ThemedJList(T[] listData) {
		super(listData);
	}

	public ThemedJList(Vector<? extends T> listData) {
		super(listData);
	}

	{
		setForeground(UIManager.getColor("List.foreground"));
		Color c = UIManager.getColor("List.background");
		if (c != null) {
			//HACK for some reason, direct passing not work in GTK LaF
			setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue()));
		}
	}
}
