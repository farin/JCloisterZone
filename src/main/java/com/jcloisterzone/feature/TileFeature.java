package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public abstract class TileFeature implements Feature {

	private int id; //unique feature identifier
	private Tile tile;
	private Location location;
	private Feature[] neighbouring;

	private Meeple meeple;

	protected Game getGame() {
		return tile.getGame();
	}

	@Override
	public boolean isOccupied() {
		return meeple != null;
	}
	@Override
	public boolean isOccupiedBy(Player p) {
		return meeple != null && meeple.getPlayer() == p;
	}
	@Override
	public boolean isOccupiedBy(Class<? extends Meeple> clazz) {
		return clazz.isInstance(meeple);
	}

	@Override
	public boolean isFeatureOccupied() {
		return isOccupied();
	}
	@Override
	public boolean isFeatureOccupiedBy(Player p) {
		return isOccupiedBy(p);
	}
	@Override
	public boolean isFeatureOccupiedBy(Class<? extends Meeple> clazz) {
		return isOccupiedBy(clazz);
	}


	@Override
	public void walk(FeatureVisitor visitor) {
		visitor.visit(this);
	}
	@Override
	public Feature getRepresentativeFeature() {
		return this;
	}

//	@Override
//	public void deployMeeple(Meeple meeple) {
//		this.meeple = meeple;
//		getGame().fireGameEvent().deployed(meeple);
//	}
//	@Override
//	public void undeployMeeple() {
//		if (meeple != null) {
//			getGame().fireGameEvent().undeployed(meeple);
//			meeple.clearDeployment();
//			meeple = null;
//		}
//	}


	public void setMeeple(Meeple meeple) {
		this.meeple = meeple;
	}

	public Meeple getMeeple() {
		return meeple;
	}


	public Feature[] getNeighbouring() {
		return neighbouring;
	}
	public void setNeighbouring(Feature[] neighbouring) {
		this.neighbouring = neighbouring;
	}


	public Tile getTile() {
		return tile;
	}
	public void setTile(Tile tile) {
		assert this.tile == null;
		this.tile = tile;
	}

	public Location getLocation() {
		return location.rotateCW(tile.getRotation());
	}
	public void setLocation(Location location) {
		assert this.location == null;
		this.location = location;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+getId();
	}

}
