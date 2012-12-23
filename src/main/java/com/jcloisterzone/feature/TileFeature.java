package com.jcloisterzone.feature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
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

    private List<Meeple> meeples = Collections.emptyList();

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

    @Override
    public void addMeeple(Meeple meeple) {
        if (meeples.isEmpty()) {
            meeples = Collections.singletonList(meeple);
            return;
        } else {
            //rare case (eg. Crop circles allows this) when more then one followe stay on same feature
            meeples = Lists.newLinkedList(meeples);
            meeples.add(meeple);
        }
    }

    @Override
    public void removeMeeple(Meeple meeple) {
        if (meeples.size() == 1) {
            assert meeples.get(0) == meeple;
            meeples = Collections.emptyList();
        } else {
            meeples.remove(meeple);
        }
    }

    @Override
    public List<Meeple> getMeeples() {
        return meeples;
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

    public Location getRawLocation() {
        return location;
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
