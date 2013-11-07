package com.jcloisterzone.ui.theme;

import java.awt.Color;
import java.awt.Image;
import java.net.URL;
import java.util.Collections;

import com.google.common.collect.Iterables;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.Client;

public class FigureTheme extends Theme {

	public FigureTheme(Client client) {
		super("theme-figures", client);
	}

	public Image getActionImage(PlayerAction action, Color color) {
		return getLayeredImage("actions/" + action.getName(), color);
	}

	public Image getNeutralImage(String name) {
		return getLayeredImage("neutral/" + name, null);
	}

	public Image getFigureImage(Meeple m, Color c) {
		return getFigureImage(m.getClass(), c, null);
	}

	public Image getFigureImage(Class<? extends Meeple> type, Color c, String extraDecoration) {
		String name = type.getSimpleName().toLowerCase();
		return getFigureImage("player-meeples/" + name, c, extraDecoration, null);
	}

	public Image getTunnelImage(Color c) {
		return getFigureImage("player-meeples/tunnel" , c, null, null);
	}

	public Image getPlayerSlotImage(String name, Color c) {
		return getFigureImage("player-slot/" + name, c, null, 60);
	}

	private Image getFigureImage(String name, Color c, String extraDecoration, Integer fixedSize) {
		String key = name + '#' + c.getRGB() + '#' + extraDecoration;
		Image image = getImageCache().get(name);
		if (image == null) {
			Iterable<URL> layers = getResourceLayers(name);
			if (extraDecoration != null) {
				URL url = getResource("player-meeples/decorations/" + extraDecoration);
				layers = Iterables.concat(layers, Collections.singletonList(url));
			}
			image = composeImages(layers, c);
			if (fixedSize != null) {
				image = image.getScaledInstance(fixedSize, fixedSize, Image.SCALE_SMOOTH);
			}

			getImageCache().put(key, image);
		}
		return image;

	}

}
