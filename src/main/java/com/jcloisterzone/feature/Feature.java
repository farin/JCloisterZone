package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Meeple;

public interface Feature {

	int getId();

	boolean isOccupied();
	boolean isOccupiedBy(Player p);
	boolean isOccupiedBy(Class<? extends Meeple> clazz);

	boolean isFeatureOccupied();
	boolean isFeatureOccupiedBy(Player p);
	boolean isFeatureOccupiedBy(Class<? extends Meeple> clazz);

	Location getLocation();
	Tile getTile();
	Feature[] getNeighbouring();

	void setMeeple(Meeple meeple);
	Meeple getMeeple();

	void walk(FeatureVisitor visitor);
	Feature getRepresentativeFeature();

}
