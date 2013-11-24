package com.jcloisterzone.game;

import java.io.Serializable;

/**
 * Useful information for client about game
 *
 */
@Deprecated
public class AdditionalInfo implements Serializable {

	private static final long serialVersionUID = 5454689251221437899L;

	protected int completedCities, biggestCitySize;
	protected int completedRoads, longestRoadLength;

	public int getCompletedCities() {
		return completedCities;
	}

	public int getBiggestCitySize() {
		return biggestCitySize;
	}

	public int getCompletedRoads() {
		return completedRoads;
	}

	public int getLongestRoadLength() {
		return longestRoadLength;
	}

	public void setCompletedCities(int completedCities) {
		this.completedCities = completedCities;
	}

	public void setBiggestCitySize(int biggestCitySize) {
		this.biggestCitySize = biggestCitySize;
	}

	public void setCompletedRoads(int completedRoads) {
		this.completedRoads = completedRoads;
	}

	public void setLongestRoadLength(int longestRoadLength) {
		this.longestRoadLength = longestRoadLength;
	}

}
