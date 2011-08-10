package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcloisterzone.game.Game;


/**
 * Board represent game board with any size, so <code>Tile</code> instances ale
 * stored in <code>Map</code>. Board supplies proper merging of score objects
 * (<code>Road</code>, <code>City</code> or <code>Farm</code>)
 * and diagonal merge for <code>Cloister<code> instances.

 */
public class Board {
	protected final Map<Position,Tile> tiles = new LinkedHashMap<Position,Tile>();
	protected final Map<Position, EdgePattern> availMoves = Maps.newHashMap();
	protected final Map<Position, Set<Rotation>> currentAvailMoves = Maps.newHashMap();
	protected final Set<Position> holes = Sets.newHashSet();

	private int maxX, minX, maxY, minY;

	private final Game game;

//	protected Set<TunnelEnd> tunnels = Sets.newHashSet();
//	protected Map<Integer, TunnelEnd> openTunnels = Maps.newHashMap(); //tunnel with open one side

	protected List<String> discardedTiles = Lists.newArrayList();


	public Board(Game game) {
		this.game = game;
	}

	/**
	 * Updates current avail moves for next turn
	 * @param tile next tile
	 */
	public void checkMoves(Tile tile) {
		currentAvailMoves.clear();
		for (Position p : availMoves.keySet()) {
			EnumSet<Rotation> allowed = EnumSet.noneOf(Rotation.class);
			for(Rotation rotation: Rotation.values()) {
				tile.setRotation(rotation);
				if (! checkPlacement(tile, p)) continue;
				if (! game.expansionDelegate().checkMove(tile, p)) continue;
				allowed.add(rotation);
			}
			if (! allowed.isEmpty()) {
				currentAvailMoves.put(p, allowed);
			}
		}
		tile.setRotation(Rotation.R0); //reset rotation
	}


	protected void availMovesAdd(Position pos) {
		availMoves.put(pos, EdgePattern.forEmptyTile(this, pos));
	}

	protected void availMovesRemove(Position pos) {
		availMoves.remove(pos);
	}

	public EdgePattern getAvailMoveEdgePattern(Position pos) {
		return availMoves.get(pos);
	}


	/**
	 * Place tile on given position. Check for correct placement (check if neigbours
	 * edges match with tile edges according to Carcassonne rules
	 * @param tile tile to place
	 * @param p position to place
	 * @throws IllegalMoveException if placement is violate game rules
	 */
	public void add(Tile tile, Position p) {
		add(tile, p, false);
	}

	public void add(Tile tile, Position p, boolean unchecked) {
		if (! unchecked) {
			if (tile.isAbbeyTile()) {
				if (! holes.contains(p)) {
					throw new IllegalArgumentException("Abbey must be placed inside hole");
				}
			} else {
				if (! currentAvailMoves.containsKey(p)) {
					throw new IllegalArgumentException("Invalid position");
				}
				if (! currentAvailMoves.get(p).contains(tile.getRotation())) {
					throw new IllegalArgumentException("Incorrect rotation");
				}
			}
		}
//		if (game.hasExpansion(Expansion.TUNNEL)) {
//			//TODO enumerate features with single iteration
//			for(Location loc : Location.sides()) {
//				Road road = (Road) tile.getFeaturePartOf(loc, Road.class);
//				if (road != null && road.isTunnelEnd()) {
//					tunnels.add(new TunnelEnd(p, loc));
//				}
//			}
//		}

		for(Entry<Location, Tile> e : getSideTilesMap(p.x, p.y).entrySet()) {
			tile.merge(e.getValue(), e.getKey());
		}

		tiles.put(p, tile);
		availMovesRemove(p);

		for(Location d : Location.sides()) {
			Position next = p.add(d);
			if (get(next) == null) {
				availMovesAdd(next);
				if (checkHole(next)) {
					holes.add(next);
				}
			}
		}
		holes.remove(p);
		tile.setPosition(p);
		if (p.x > maxX) maxX = p.x;
		if (p.x < minX) minX = p.x;
		if (p.y > maxY) maxY = p.y;
		if (p.y < minY) minY = p.y;
	}

	public void discardTile(String tileId) {
		discardedTiles.add(tileId);
		game.fireGameEvent().tileDiscarded(tileId);
	}

	public List<String> getDiscardedTiles() {
		return discardedTiles;
	}

	private boolean checkHole(Position p) {
		for(Location d : Location.sides()) {
			Position next = p.add(d);
			if (get(next) == null) {
				return false;
			}
		}
		return true;
	}

	public Map<Position, Set<Rotation>> getAvailablePlacements() {
		return currentAvailMoves;
	}

	public Set<Position> getAvailablePlacementPositions() {
		return currentAvailMoves.keySet();
	}

	public Set<Position> getHoles() {
		return holes;
	}


	/**
	 * Returns tile on position with cordinates <code>x</code>,<code>y</code>.
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return demand tile
	 */
	public Tile get(int x, int y) {
		return tiles.get(new Position(x, y));
	}

	public Tile get(Position p) {
		return tiles.get(p);
	}

	public Collection<Tile> getAllTiles() {
		return tiles.values();
	}

	/*
	 * Check if placement is legal against orthonogal neigbours. */
	private boolean checkPlacement(Tile tile, Position p) {
		for (Entry<Location, Tile> e : getSideTilesMap(p.x, p.y).entrySet()) {
			if (! tile.check(e.getValue(), e.getKey(), this)) {
				return false;
			}
		}
		return true;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinX() {
		return minX;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getMinY() {
		return minY;
	}

	public List<Tile> getSideTiles(int x, int y) {
		return getTilesForSides(x, y, Location.sides());
	}

	public Map<Location, Tile> getSideTilesMap(int x, int y) {
		return getTilesForSidesMap(x, y, Location.sides());
	}

	public List<Tile> getDiagonalTiles(int x, int y) {
		return getTilesForSides(x, y, Location.sidesDiagonal());
	}

	public Map<Location, Tile> getDiagonalTilesMap(int x, int y) {
		return getTilesForSidesMap(x, y, Location.sidesDiagonal());
	}

	public List<Tile> getAllNeigbourTiles(int x, int y) {
		List<Tile> l = getTilesForSides(x, y, Location.sides());
		l.addAll(getTilesForSides(x, y, Location.sidesDiagonal()));
		return l;
	}

	public Map<Location, Tile> getAllNeigbourTilesMap(int x, int y) {
		Map<Location, Tile> m = getTilesForSidesMap(x, y, Location.sides());
		m.putAll(getTilesForSidesMap(x, y, Location.sidesDiagonal()));
		return m;
	}

	private List<Tile> getTilesForSides(int x, int y, Location[] sides) {
		List<Tile> tiles = new ArrayList<Tile>(4);
		for(Location d : sides) {
			Position p = (new Position(x,y)).add(d);
			Tile t = get(p);
			if (t != null) {
				tiles.add(t);
			}
		}
		return tiles;
	}

	private Map<Location, Tile> getTilesForSidesMap(int x, int y, Location[] sides) {
		Map<Location, Tile> tiles = new HashMap<Location, Tile>(4);
		for(Location d : sides) {
			Position p = (new Position(x,y)).add(d);
			Tile t = get(p);
			if (t != null) {
				tiles.put(d, t);
			}
		}
		return tiles;
	}

//	public Set<TunnelEnd> getTunnels() {
//		return tunnels;
//	}
//
//	public static class TunnelEnd {
//		final public Position pos;
//		final public Location loc;
//
//		public TunnelEnd(Position pos, Location dir) {
//			this.pos = pos;
//			this.loc = dir;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (!(obj instanceof TunnelEnd)) return false;
//			TunnelEnd te = (TunnelEnd) obj;
//			return pos.equals(te.pos) && loc.equals(te.loc);
//		}
//
//		@Override
//		public int hashCode() {
//			return 43 * pos.hashCode() + loc.hashCode();
//		}
//
//	}



}
