package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BridgeCapability;


/**
 * Board represent game board with any size, so <code>Tile</code> instances ale
 * stored in <code>Map</code>. Board supplies proper merging of score objects
 * (<code>Road</code>, <code>City</code> or <code>Farm</code>)
 * and diagonal merge for <code>Cloister<code> instances.

 */
public class Board {
    protected final Map<Position,Tile> tiles = new LinkedHashMap<Position,Tile>();
    protected final Map<Position, EdgePattern> availMoves = new HashMap<>();
    protected final Map<Position, Set<Rotation>> currentAvailMoves = new HashMap<>();
    protected final Set<Position> holes = new HashSet<>();

    private int maxX, minX, maxY, minY;

    private final Game game;

//	protected Set<TunnelEnd> tunnels = new HashSet<>();
//	protected Map<Integer, TunnelEnd> openTunnels = new HashMap<>(); //tunnel with open one side

    protected List<Tile> discardedTiles = new ArrayList<>();


    public Board(Game game) {
        this.game = game;
    }

    /**
     * Updates current avail moves for next turn
     * @param tile next tile
     */
    public void refreshAvailablePlacements(Tile tile) {
        Rotation tileRotation = tile.getRotation();
        currentAvailMoves.clear();
        for (Position p : availMoves.keySet()) {
            EnumSet<Rotation> allowed = EnumSet.noneOf(Rotation.class);
            for (Rotation rotation: Rotation.values()) {
                tile.setRotation(rotation);
                if (!isPlacementAllowed(tile, p)) {
                    //not allowed according standard rules, must check if deployed bridge can allow it
                    if (!game.hasCapability(BridgeCapability.class)) continue;
                    if (!game.getCapability(BridgeCapability.class).isTilePlacementWithBridgePossible(tile, p)) continue;
                }
                if (!game.isTilePlacementAllowed(tile, p)) continue;
                allowed.add(rotation);
            }
            if (!allowed.isEmpty()) {
                currentAvailMoves.put(p, allowed);
            }
        }
        tile.setRotation(tileRotation); //reset rotation
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
        if (!unchecked) {
            if (tile.isAbbeyTile()) {
                if (!holes.contains(p)) {
                    throw new IllegalArgumentException("Abbey must be placed inside hole");
                }
            } else {
                if (!currentAvailMoves.containsKey(p)) {
                    throw new IllegalArgumentException("Invalid position " + p);
                }
                if (!currentAvailMoves.get(p).contains(tile.getRotation())) {
                    throw new IllegalArgumentException("Incorrect rotation " + tile.getRotation() + " "+ p);
                }
            }
        }

        tiles.put(p, tile);
        availMovesRemove(p);

        for (Position offset: Position.ADJACENT.values()) {
            Position next = p.add(offset);
            if (get(next) == null) {
                availMovesAdd(next);
                if (isHole(next)) {
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

    public void mergeFeatures(Tile tile) {
        for (Entry<Location, Tile> e : getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            tile.merge(e.getValue(), e.getKey());
        }
    }

    public void remove(Tile tile) {
        Position pos = tile.getPosition();
        assert pos != null;
        tiles.remove(pos);
        tile.setPosition(null);
        availMovesAdd(pos);
        if (isHole(pos)) holes.add(pos);
        for (Position offset: Position.ADJACENT.values()) {
            Position next = pos.add(offset);
            holes.remove(next);
            if (getAdjacentCount(next) == 0) {
                availMoves.remove(next);
            }
        }
    }

    public void unmergeFeatures(Tile tile) {
        assert tile.getPosition() != null;
        for (Entry<Location, Tile> e : getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            tile.unmerge(e.getValue(), e.getKey());
        }
    }

    public void discardTile(Tile tile) {
        discardedTiles.add(tile);
        game.fireGameEvent().tileDiscarded(tile);
    }


    public List<Tile> getDiscardedTiles() {
        return discardedTiles;
    }

    private boolean isHole(Position p) {
        for (Position offset: Position.ADJACENT.values()) {
            Position next = p.add(offset);
            if (get(next) == null) {
                return false;
            }
        }
        return true;
    }

    private int getAdjacentCount(Position p) {
        int count = 0;
        for (Position offset: Position.ADJACENT.values()) {
            Position next = p.add(offset);
            if (get(next) != null) {
                count++;
            }
        }
        return count;
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
    public boolean isPlacementAllowed(Tile tile, Position p) {
        for (Entry<Location, Tile> e : getAdjacentTilesMap(p).entrySet()) {
            if (!tile.check(e.getValue(), e.getKey(), this)) {
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

    public List<Tile> getMulti(Position[] positions) {
        List<Tile> tiles = new ArrayList<>();
        for (Position p : positions) {
            Tile t = get(p);
            if (t != null) {
                tiles.add(t);
            }
        }
        return tiles;
    }

    public Map<Location, Tile> getAdjacentTilesMap(Position pos) {
        Map<Location, Tile> tiles = new HashMap<Location, Tile>(4);
        for (Entry<Location, Position> e: Position.ADJACENT.entrySet()) {
            Tile tile = get(e.getValue().add(pos));
            if (tile != null) {
                tiles.put(e.getKey(), tile);
            }
        }
        return tiles;
    }

    public List<Tile> getAdjacentAndDiagonalTiles(Position pos) {
        return getMulti(pos.addMulti(Position.ADJACENT_AND_DIAGONAL.values()));
    }

}
