package com.jcloisterzone.ui.theme;

import java.awt.Image;

import com.jcloisterzone.ui.Client;

public class ControlsTheme extends Theme {

	public ControlsTheme(Client client) {
		super("theme-controls", client);
	}

	public Image getActionDecoration(String name) {
		return getImage("action-decorations/" + name + ".png");
	}


}
