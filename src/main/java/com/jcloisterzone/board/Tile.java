package com.jcloisterzone.board;

import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.Player;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.game.Game;


/**
 * Represents one game tile. Contains references on score objects
 * which lays on tile and provides logic for tiles merging, rotating
 * and placing figures.
 *
 * @author farin
 */
public class Tile /*implements Cloneable*/ {

	public static final String ABBEY_TILE_ID = "AM.A";

	protected Game game;
	private final String id;

	private Feature[] features;
	private Location river;

	protected TileSymmetry symmetry;
	protected Position position = null;
	private Rotation rotation = Rotation.R0;

	private TileTrigger trigger;
	private EdgePattern edgePattern;

	public Tile(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}



//	public char getSideMaskAt(Location loc) {
//		return sideMask.charAt(loc.rotateCCW(rotation).ordinal());
//	}


	public EdgePattern getEdgePattern() {
		return edgePattern;
	}

	public void setEdgePattern(EdgePattern edgePattern) {
		this.edgePattern = edgePattern;
	}

	/**
	 * Gets tile ID.
	 * @return tile ID.
	 */
	public String getId() {
		return id;
	}

	protected boolean check(Tile tile, Location rel, Board board) {
		return edgePattern.at(rel, rotation) == tile.edgePattern.at(rel.rev(), tile.rotation);
	}

	public void setFeatures(Feature[] features) {
		assert this.features == null;
		this.features = features;
	}

	public Feature[] getFeatures() {
		return features;
	}

	public Feature getFeature(Location loc) {
		for(Feature p : features) {
			if (p.getLocation().equals(loc)) return p;
		}
		return null;
	}

	public Feature getFeaturePartOf(Location loc, Class<?/* extends FeaturePiece*/>... allowedClasses) {
		assert allowedClasses.length > 0;
		for(Feature p : features) {
			if (loc.isPartOf(p.getLocation())) {
				for(Class<?> clazz : allowedClasses) {
					if (clazz.isInstance(p)) return p;
				}
			}
		}
		return null;
	}

	/** merge this to another tile - method argument is tile placed before */
	protected void merge(Tile tile, Location loc) {
		MultiTileFeature oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(loc.rev(), Road.class, City.class);
		int farmLoopBound = 1;
		if (oppositePiece != null) {
			if (isAbbeyTile()) {
				oppositePiece.setAbbeyEdge(loc.rev());
			} else {
				MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(loc, Road.class, City.class);
				oppositePiece.setEdge(loc.rev(), thisPiece);
				thisPiece.setEdge(loc, oppositePiece);
			}
			farmLoopBound = 2; //farm can be divided in two parts by road
		}
		for(int i = 0; i < farmLoopBound; i++) {
			Location halfSide = loc.farmHalfSide(i);
			oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(halfSide.rev(), Farm.class);
			if (oppositePiece != null) {
				if (isAbbeyTile()) {
					oppositePiece.setAbbeyEdge(loc.rev());
				} else {
					MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(halfSide, Farm.class);
					oppositePiece.setEdge(loc.rev(), thisPiece);
					thisPiece.setEdge(loc, oppositePiece);
				}
			}
		}
	}

	protected void rotate() {
		rotation = rotation.next();
	}

	public void setRotation(Rotation rotation) {
		assert rotation != null;
		this.rotation =  rotation;
	}

	public Rotation getRotation() {
		return rotation;
	}

	public TileSymmetry getSymmetry() {
		return symmetry;
	}

	public void setSymmetry(TileSymmetry symmetry) {
		this.symmetry = symmetry;
	}



	public boolean isAbbeyTile() {
		return id.equals(ABBEY_TILE_ID);
	}

	public boolean hasCloister() {
		return getFeature(Location.CLOISTER) != null;
	}


	public Cloister getCloister() {
		return (Cloister) getFeature(Location.CLOISTER);
	}

	public Tower getTower() {
		return (Tower) getFeature(Location.TOWER);
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}


	protected void setPosition(Position p) {
		position = p;
	}

	public Position getPosition() {
		return position;
	}

	private static class UnoccupiedScoreableVisitor implements FeatureVisitor {
		private boolean completed = true, occupied;

		@Override
		public boolean visit(Feature feature) {
			if (feature.getMeeple() != null) {
				occupied = true;
				return false;
			}
			if (feature instanceof Completable) {
				if (! ((Completable)feature).isPieceCompleted()) {
					completed = false;
				}
			}
			return true;
		}

		public boolean isOccupied() {
			return occupied;
		}

		public boolean isCompleted() {
			return completed;
		}
	}


	public Set<Location> getUnoccupiedScoreables(boolean excludeCompleted) {
		Set<Location> locations = Sets.newHashSet();
		for(Feature f : features) {
			if (f instanceof Scoreable) {
				UnoccupiedScoreableVisitor visitor = new UnoccupiedScoreableVisitor();
				f.walk(visitor);
				if (visitor.isOccupied()) continue;
				if (excludeCompleted && f instanceof Completable && visitor.isCompleted()) continue;
				locations.add(f.getLocation());
			}
		}
		return locations;
	}


	public Set<Location> getPlayerFeatures(Player player, Class<? extends Feature> featureClass) {
		Set<Location> locations = Sets.newHashSet();
		for(Feature f : features) {
			if (! featureClass.isInstance(f)) continue;
			if (f.isFeatureOccupiedBy(player)) {
				locations.add(f.getLocation());
			}
		}
		return locations;
	}

	@Override
	public String toString() {
		return getId() + '(' + getRotation() + ')';
	}

	public TileTrigger getTrigger() {
		return trigger;
	}

	public void  setTrigger(TileTrigger trigger) {
		this.trigger = trigger;
	}

	public City getPrincessCityPiece() {
		for(Feature p : features) {
			if (p instanceof City ) {
				City cp = (City) p;
				if (cp.isPricenss()) {
					return cp;
				}
			}
		}
		return null;
	}



	public Location getRiver() {
		return river;
	}

	public void setRiver(Location river) {
		this.river = river;
	}

}
