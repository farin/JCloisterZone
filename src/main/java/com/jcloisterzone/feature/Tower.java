package com.jcloisterzone.feature;


public class Tower extends TileFeature {

	private int height;

	public int increaseHeight() {
		return ++height;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}


}
