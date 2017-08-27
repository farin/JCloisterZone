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

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.feature.Feature;
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

    /**
     * Constructs a new {@code Board}.
     * @param game the game this board belongs to
     */
    public Board(Game game) {
        this.game = game;
    }

    /**
     * Updates current available moves for the next turn given a tile.
     * @param tile the tile to be place in the next turn
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

    /**
     * Adds {@code pos} to the list of unoccupied positions currently touching some tile.
     * @param pos the position to add
     */
    protected void availMovesAdd(Position pos) {
        availMoves.put(pos, EdgePattern.forEmptyTile(this, pos));
    }

    /**
     * Removes {@code pos} from the list of unoccupied positions currently touching some tile.
     * @param pos the position to remove
     */
    protected void availMovesRemove(Position pos) {
        availMoves.remove(pos);
    }

    /**
     * Gets the {@link EdgePattern}  of a given position. This represents the constraints that a tile
     * needs to satisfy to be placed in {@code pos}.
     *
     * @param pos the position of interest
     * @return the edge pattern of the position of interest
     */
    public EdgePattern getAvailMoveEdgePattern(Position pos) {
        return availMoves.get(pos);
    }

    /**
     * Places {@code tile} on position {@code p}. Prior to placing, checks if neighbours
     * edges match with {@code tile} edges according to Carcassonne rules. If rules
     * are violated, raises {@link IllegalArgumentException}.
     * @param tile the tile to place
     * @param p the position where to place the tile
     * @throws IllegalArgumentException if placement is violate game rules
     */
    public void add(Tile tile, Position p) {
        add(tile, p, false);
    }

    /**
     * Places {@code tile} on {@code p}. If {@code unchecked} is false, prior to placing, checks if neighbours
     * edges match with {@code tile} edges according to Carcassonne rules. If rules
     * are violated, raises {@link IllegalArgumentException}. Otherwise, violations are ignored.
     * @param tile the tile to place
     * @param p the position where to place the tile
     * @param unchecked whether to check game rules for violations
     * @throws IllegalArgumentException if placement is violate game rules
     */
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

    /**
     * Merges the features of {@code this} and {@code tile}, so that they reference each other.
     * @param tile the tile to merge
     */
    public void mergeFeatures(Tile tile) {
        for (Entry<Location, Tile> e : getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            tile.merge(e.getValue(), e.getKey());
        }
    }

    /**
     * Removes {@code tile} from the board.
     * @param tile the tile to remove
     */
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

    /**
     * Un-merges the features of {@code this} and {@code tile}, so that they do not reference each other any more.
     * @param tile the tile to un-merge
     */
    public void unmergeFeatures(Tile tile) {
        assert tile.getPosition() != null;
        for (Entry<Location, Tile> e : getAdjacentTilesMap(tile.getPosition()).entrySet()) {
            tile.unmerge(e.getValue(), e.getKey());
        }
    }

    /**
     * Discards {@code tile}.
     * @param tile the tile to be discarded
     */
    public void discardTile(Tile tile) {
        discardedTiles.add(tile);
        game.post(new TileEvent(TileEvent.DISCARD, null, tile, null));
    }

    /**
     * Returns a {@link List} of all the tiles discarded so far in the game.
     * @return a {@link List} of all the tiles discarded so far in the game
     */
    public List<Tile> getDiscardedTiles() {
        return discardedTiles;
    }

    /**
     * Check if position {@code p} is a hole (i.e., is surrounded by tiles on all sides).
     * @param p the position to check tiles around
     * @return {@code true} if position {@code p} is a hole, {@code false} otherwise
     */
    private boolean isHole(Position p) {
        for (Position offset: Position.ADJACENT.values()) {
            Position next = p.add(offset);
            if (get(next) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts the number of tiles adjacent to position {@code p}.
     * @param p the position to count tiles around
     * @return the number of tiles adjacent to position {@code p}
     */
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

    /**
     * Returns a {@link Set} containing all the legal placements. A placement includes both a {@link Position} and a
     * {@link Rotation}. This method is intended to be called after {@link #refreshAvailablePlacements}.
     * @return a {@link Set} all the positions where placement is legal
     */
    public Map<Position, Set<Rotation>> getAvailablePlacements() {
        return currentAvailMoves;
    }

    /**
     * Returns a {@link Set} containing all the positions where a placement is legal. This method is intended to be called
     * after {@link #refreshAvailablePlacements}.
     * @return a {@link Set} all the positions where a placement is legal
     */
    public Set<Position> getAvailablePlacementPositions() {
        return currentAvailMoves.keySet();
    }

    /**
     * Returns a {@link Set} containing the positions of all the holes in the map (i.e., locations surrounded by tiles
     * on all sides).
     * @return a {@link Set} containing the positions of all the holes in the map
     */
    public Set<Position> getHoles() {
        return holes;
    }

    /**
     * Returns tile in position with coordinates {@code x}, {@code y}.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the tile in position with coordinates {@code x}, {@code y}
     */
    public Tile get(int x, int y) {
        return tiles.get(new Position(x, y));
    }

    /**
     * Returns tile in position {@code p}.
     * @param p the position to retrieve the tile from
     * @return the tile in position {@code p}
     */
    public Tile get(Position p) {
        return tiles.get(p);
    }

    /**
     * Returns the {@link Feature} pointed to by {@code fp}, or null if there is no tile in the pointed position or no
     * feature in the specified location on the specified tile.
     * @param fp the pointer to the feature of interest
     * @return the {@link Feature} pointed to by {@code fp}, or null if there is no such {@link Feature}
     */
    public Feature get(FeaturePointer fp) {
        Tile tile =  tiles.get(fp.getPosition());
        return tile == null ? null : tile.getFeaturePartOf(fp.getLocation());
    }

    /**
     * Returns a {@link Collection} with all the tiles on the board.
     * @return a {@link Collection} with all the tiles on the board
     */
    public Collection<Tile> getAllTiles() {
        return tiles.values();
    }

    /**
     * Checks if the placement of {@code tile} in position {@code p} is legal.
     *
     * @param p the position to place the tile
     * @param tile the tile to place
     * @return {@code true} if the placement is legal, {@code false} otherwise
     */
    public boolean isPlacementAllowed(Tile tile, Position p) {
        for (Entry<Location, Tile> e : getAdjacentTilesMap(p).entrySet()) {
            if (!tile.check(e.getValue(), e.getKey(), this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the X coordinate of the rightmost tile.
     * @return the X coordinate of the rightmost tile
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Returns the X coordinate of the leftmost tile.
     * @return the X coordinate of the leftmost tile
     */
    public int getMinX() {
        return minX;
    }

    /**
     * Returns the Y coordinate of the topmost tile.
     * @return the Y coordinate of the topmost tile
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Returns the Y coordinate of the bottommost tile.
     * @return the Y coordinate of the bottommost tile
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Returns a {@link List} of the tiles in {@code positions} (positions without tiles are skipped).
     * @param positions the positions to fetch the tiles from
     * @return a {@link List} of tiles, one for each entry in {@code positions}, except for those with no tile
     */
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

    /**
     * Returns a {@link Map} of the tiles surrounding {@code pos}. The map has 4 or less keys, one for each
     * {@link Position#ADJACENT} unless the location is empty.
     * @param pos the position around which to search
     * @return the map of surrounding tiles
     */
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

    /**
     * Returns a {@link List} of the tiles surrounding {@code pos}, including diagonally. Positions without tiles are
     * skipped
     * @param pos the position around which to search
     * @return the map of surrounding tiles
     */
    public List<Tile> getAdjacentAndDiagonalTiles(Position pos) {
        return getMulti(pos.addMulti(Position.ADJACENT_AND_DIAGONAL.values()));
    }

    /**
     * Counts the number of tiles in a given {@code direction} starting from {@code start}.
     * @param start the position to start from
     * @param direction the direction to move
     * @return the number of tiles in a given {@code direction} starting from {@code start}
     */
    public int getContinuousRowSize(Position start, Location direction) {
        start = start.add(direction);
        int size = 0;
        while (get(start) != null) {
            size++;
            start = start.add(direction);
        }
        return size;
    }

}
