package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.game.Game;


/**
 * Represents one game tile. Contains references on score objects
 * which lays on tile and provides logic for tiles merging, rotating
 * and placing figures.
 *
 * @author Roman Krejcik
 */
public class Tile /*implements Cloneable*/ {

	public static final String ABBEY_TILE_ID = "AM.A";

	protected Game game;
	private final String id;

	private ArrayList<Feature> features;	
	private Bridge bridge; //direct ref to bridge feature
	private Location river;

	protected TileSymmetry symmetry;
	protected Position position = null;
	private Rotation rotation = Rotation.R0;
	
	/** no dragon, no magic gate, not bridges for forbidden tiles (used for initial Count tiles) */
	private boolean forbidden;	

	private TileTrigger trigger;
	private EdgePattern edgePattern;

	public Tile(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public boolean isForbidden() {
		return forbidden;
	}

	public void setForbidden(boolean forbidden) {
		this.forbidden = forbidden;
	}

	public EdgePattern getEdgePattern() {
		return edgePattern;
	}

	public void setEdgePattern(EdgePattern edgePattern) {
		this.edgePattern = edgePattern;
	}

	public String getId() {
		return id;
	}

	protected boolean check(Tile tile, Location rel, Board board) {
		return getEdgePattern().at(rel, rotation) == tile.getEdgePattern().at(rel.rev(), tile.rotation);
	}

	public void setFeatures(ArrayList<Feature> features) {
		assert this.features == null;
		this.features = features;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public Feature getFeature(Location loc) {
		for(Feature p : features) {
			if (p.getLocation().equals(loc)) return p;
		}
		return null;
	}

	public Feature getFeaturePartOf(Location loc) {
		for(Feature p : features) {
			if (loc.isPartOf(p.getLocation())) {
				return p;
			}
		}
		return null;
	}

	/** merge this to another tile - method argument is tile placed before */
	protected void merge(Tile tile, Location loc) {
		Location oppositeLoc = loc.rev();
		MultiTileFeature oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeLoc);
		if (oppositePiece != null) {
			if (isAbbeyTile()) {
				oppositePiece.setAbbeyEdge(oppositeLoc);
			} else {
				MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(loc);
				oppositePiece.setEdge(oppositeLoc, thisPiece);
				thisPiece.setEdge(loc, oppositePiece);
			}
		}
		for(int i = 0; i < 2; i++) {
			Location halfSide = i == 0 ? loc.getLeftFarm() : loc.getRightFarm();
			Location oppositeHalfSide = halfSide.rev();
			oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeHalfSide);
			if (oppositePiece != null) {
				if (isAbbeyTile()) {
					oppositePiece.setAbbeyEdge(oppositeHalfSide);
				} else {
					MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(halfSide);
					oppositePiece.setEdge(oppositeHalfSide, thisPiece);
					thisPiece.setEdge(halfSide, oppositePiece);
				}
			}
		}
	}

	protected void unmerge(Tile tile, Location loc) {
		Location oppositeLoc = loc.rev();
		MultiTileFeature oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeLoc);
		if (oppositePiece != null) {
			oppositePiece.setEdge(oppositeLoc, null);
			if (! isAbbeyTile()) {
				MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(loc);
				thisPiece.setEdge(loc, null);
			}
		}
		for(int i = 0; i < 2; i++) {
			Location halfSide = i == 0 ? loc.getLeftFarm() : loc.getRightFarm();
			Location oppositeHalfSide = halfSide.rev();
			oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeHalfSide);
			if (oppositePiece != null) {
				oppositePiece.setEdge(oppositeHalfSide, null);
				if (! isAbbeyTile()) {
					MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(halfSide);
					thisPiece.setEdge(halfSide, null);
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


	public void setPosition(Position p) {
		position = p;
	}

	public Position getPosition() {
		return position;
	}
	
	
	public Bridge getBridge() {
		return bridge;
	}

	public void placeBridge(Location bridgeLoc) {
		assert bridge == null && bridgeLoc != null; //TODO AI support - remove bridge from tile
		bridge = new Bridge();
		bridge.setId(game.idSequnceNextVal());
		bridge.setTile(this);		
		bridge.setLocation(bridgeLoc.rotateCCW(rotation));
		features.add(bridge);		
		edgePattern = edgePattern.getBridgePattern(bridgeLoc); 
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
	
	public Set<Location> getAllowedBridges() {
		if (isForbidden() || getBridge() != null) return null;
		Set<Location> allowed = null;
		if (edgePattern.isBridgeAllowed(Location.NS, rotation)) {
			allowed = Sets.newHashSet();
			allowed.add(Location.NS);
		}
		if (edgePattern.isBridgeAllowed(Location.WE, rotation)) {
			if (allowed == null) allowed = Sets.newHashSet();
			allowed.add(Location.WE);
		}
		return allowed;
	}

}
