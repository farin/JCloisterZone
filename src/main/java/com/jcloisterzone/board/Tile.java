package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.feature.visitor.IsOccupiedOrCompleted;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;


/**
 * Represents one game tile. Contains references on score objects
 * which lays on tile and provides logic for tiles merging, rotating
 * and placing figures.
 *
 * @author Roman Krejcik
 */
public class Tile /*implements Cloneable*/ {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ABBEY_TILE_ID = "AM.A";

    protected Game game;
    private final Expansion origin;
    private final String id;

    private ArrayList<Feature> features;
    private Bridge bridge; //direct reference to bridge feature

    protected TileSymmetry symmetry;
    protected Position position = null;
    private Rotation rotation = Rotation.R0;

    private EdgePattern edgePattern;

    //expansions data - maybe some map instead ? but still it is only few tiles
    private TileTrigger trigger;
    private Location river;
    private Location flier;
    private Location windRose;
    private Class<? extends Feature> cornCircle;

    public Tile(Expansion origin, String id) {
        this.origin = origin;
        this.id = id;
    }

    @Override
    public int hashCode() {
        //TODO tiles with same id has same hashcode, is it ok?
        return id.hashCode();
    }

    public EdgePattern getEdgePattern() {
        return edgePattern;
    }

    public void setEdgePattern(EdgePattern edgePattern) {
        this.edgePattern = edgePattern;
    }

    public char getEdge(Location side) {
        return getEdgePattern().at(side, rotation);
    }

    public String getId() {
        return id;
    }

    public Expansion getOrigin() {
        return origin;
    }

    protected boolean check(Tile tile, Location rel, Board board) {
        return getEdge(rel) == tile.getEdge(rel.rev());
    }

    public void setFeatures(ArrayList<Feature> features) {
        assert this.features == null;
        this.features = features;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public Feature getFeature(Location loc) {
        for (Feature p : features) {
            if (p.getLocation().equals(loc)) return p;
        }
        return null;
    }

    public Feature getFeaturePartOf(Location loc) {
        for (Feature p : features) {
            if (loc.isPartOf(p.getLocation())) {
                return p;
            }
        }
        return null;
    }

    /** merge this to another tile - method argument is tile placed before */
    protected void merge(Tile tile, Location loc) {
        //if (logger.isDebugEnabled()) logger.debug("Merging " + id + " with " + tile.getId());
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
        for (int i = 0; i < 2; i++) {
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
            if (!isAbbeyTile()) {
                MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(loc);
                thisPiece.setEdge(loc, null);
            }
        }
        for (int i = 0; i < 2; i++) {
            Location halfSide = i == 0 ? loc.getLeftFarm() : loc.getRightFarm();
            Location oppositeHalfSide = halfSide.rev();
            oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeHalfSide);
            if (oppositePiece != null) {
                oppositePiece.setEdge(oppositeHalfSide, null);
                if (!isAbbeyTile()) {
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
        Location normalizedLoc = bridgeLoc.rotateCCW(rotation);
        bridge = new Bridge();
        bridge.setId(game.idSequnceNextVal());
        bridge.setTile(this);
        bridge.setLocation(normalizedLoc);
        features.add(bridge);
        edgePattern = edgePattern.getBridgePattern(normalizedLoc);
    }

    public Set<Location> getUnoccupiedScoreables(boolean excludeCompleted) {
        Set<Location> locations = new HashSet<>();
        for (Feature f : features) {
            //if (f instanceof Farm && !game.hasCapability(Capability.FARM_PLACEMENT)) continue;
            if (f instanceof Scoreable) {
                IsOccupied visitor;
                if (excludeCompleted && f instanceof Completable) {
                    visitor = new IsOccupiedOrCompleted();
                } else {
                    visitor = new IsOccupied();
                }
                if (f.walk(visitor)) continue;
                locations.add(f.getLocation());
            }
        }
        return locations;
    }


    public Set<Location> getPlayerFeatures(Player player, Class<? extends Feature> featureClass) {
        Set<Location> locations = new HashSet<>();
        for (Feature f : features) {
            if (!featureClass.isInstance(f)) continue;
            if (f.walk(new IsOccupied().with(player).with(Follower.class))) {
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

    public void setTrigger(TileTrigger trigger) {
        this.trigger = trigger;
    }

    public boolean hasTrigger(TileTrigger trigger) {
        return trigger == this.trigger;
    }

    public Class<? extends Feature> getCornCircle() {
        return cornCircle;
    }

    public void setCornCircle(Class<? extends Feature> cornCircle) {
        this.cornCircle = cornCircle;
    }

    public City getCityWithPrincess() {
        for (Feature p : features) {
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


    public Location getFlier() {
        return flier;
    }

    public void setFlier(Location flier) {
        this.flier = flier;
    }

    public Location getWindRose() {
        return windRose;
    }

    public void setWindRose(Location windRose) {
        this.windRose = windRose;
    }

    public boolean isBridgeAllowed(Location bridgeLoc) {
        if (origin == Expansion.COUNT || getBridge() != null) return false;
        return edgePattern.isBridgeAllowed(bridgeLoc, rotation);
    }

}
