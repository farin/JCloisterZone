package com.jcloisterzone.ui.grid;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import com.jcloisterzone.ui.controls.ControlPanel;

public class MainPanelLayout implements LayoutManager {
	
	/*private final MainPanel panel;	
	
	public MainPanelLayout(MainPanel panel) {
		this.panel = panel;
	}*/

	@Override
	public void addLayoutComponent(String name, Component comp) {		
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);		
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(ControlPanel.PANEL_WIDTH, 1);	
	}

	@Override
	public void layoutContainer(Container parent) {
		Dimension size = parent.getSize();
		for(Component c : parent.getComponents()) {
			if (c instanceof GridPanel) {
				GridPanel gridPanel = (GridPanel) c;
				Dimension preferred = gridPanel.getPreferredSize();
				preferred.width = Math.min(preferred.width, size.width - ControlPanel.PANEL_WIDTH);
				preferred.height = Math.min(preferred.height, size.height);
				c.setBounds((size.width - preferred.width) / 2, (size.height - preferred.height) / 2, preferred.width, preferred.height);
			} else if (c instanceof ControlPanel) {
				c.setBounds(size.width - ControlPanel.PANEL_WIDTH, 0, ControlPanel.PANEL_WIDTH, size.height);
			}			
		}
	}

}
