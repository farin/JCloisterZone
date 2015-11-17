package com.jcloisterzone.ui.resources;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.figure.Meeple;

public class LayeredImageDescriptor {

	private String baseName;
	private Color colorOverlay;
	private String additionalLayer;

	public LayeredImageDescriptor(String baseName) {
		this(baseName, null);
	}

	public LayeredImageDescriptor(String baseName, Color colorOverlay) {
		this.baseName = baseName;
		this.colorOverlay = colorOverlay;
	}

	public LayeredImageDescriptor(Class<? extends Meeple> meeple, Color colorOverlay) {
		this("player-meeples/" + meeple.getSimpleName().toLowerCase(), colorOverlay);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(baseName);
		if (colorOverlay != null) {
			sb.append('$').append(colorOverlay.getRGB());
		}
		if (additionalLayer != null) {
			sb.append(additionalLayer);
		}
		return sb.toString();
	}

	public String getBaseName() {
		return baseName;
	}
	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}
	public Color getColorOverlay() {
		return colorOverlay;
	}
	public void setColorOverlay(Color colorOverlay) {
		this.colorOverlay = colorOverlay;
	}
	public String getAdditionalLayer() {
		return additionalLayer;
	}
	public void setAdditionalLayer(String additionalLayer) {
		this.additionalLayer = additionalLayer;
	}



}
