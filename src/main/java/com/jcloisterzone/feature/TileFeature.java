package com.jcloisterzone.feature;

import com.google.common.collect.ObjectArrays;
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
	public <T> T walk(FeatureVisitor<T> visitor) {
		visitor.visit(this);
		return visitor.getResult();
	}
	
	@Override
	public Feature getMaster() {
		return this;
	}

	public void setMeeple(Meeple meeple) {
		this.meeple = meeple;
	}

	public Meeple getMeeple() {
		return meeple;
	}

	public Feature[] getNeighbouring() {
		return neighbouring;
	}
	
	public void addNeighbouring(Feature[] neighbouring) {
		if (this.neighbouring == null) {
			this.neighbouring = neighbouring;
		} else {
			this.neighbouring = ObjectArrays.concat(this.neighbouring, neighbouring, Feature.class);
		}
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
