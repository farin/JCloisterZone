package com.jcloisterzone.ui.grid;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.ScrollPaneLayout;

public class OverlayScrollPaneLayout extends ScrollPaneLayout {

	public static final String OVERLAY = "overlay";
	
	private Component overlay;
	
	public void addLayoutComponent(String s, Component c) {
		if (OVERLAY.equals(s)) {
			overlay = c;
		} else {
			super.addLayoutComponent(s, c);
		}
	}

	@Override
	public void layoutContainer(Container parent) {		
		super.layoutContainer(parent);
		
		int rightMargin = 500; 
		if (getVerticalScrollBar().isVisible()) {
			rightMargin += getVerticalScrollBar().getWidth();
		}
		
		if (overlay != null) {
			Dimension pref = overlay.getPreferredSize();
			overlay.setBounds(parent.getWidth()-(int)pref.getWidth() - rightMargin, 0, (int)pref.getWidth(), parent.getHeight());				
		}
	}
}