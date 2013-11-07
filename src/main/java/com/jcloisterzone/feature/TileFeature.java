package com.jcloisterzone.feature;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
            meeple.setIndex(0);
        } else {
            //rare case (eg. Crop circles allows this) when more then one followe stay on same feature
            int index = -1;
            for (Meeple m : meeples) {
                if (m.getIndex() > index) index = m.getIndex();
            }
            meeples = new LinkedList<>(meeples);
            meeples.add(meeple);
            meeple.setIndex(index+1);
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
        meeple.setIndex(null);
    }

    @Override
    public final List<Meeple> getMeeples() {
        return meeples;
    }

//    @Override
//    public final Set<Class<? extends Meeple>> getMeepleTypes() {
//        if (meeples.size() == 1) {
//            return Collections.<Class<? extends Meeple>>singleton(meeples.get(0).getClass());
//        }
//        Set<Class<? extends Meeple>> types = new HashSet<>();
//        for (Meeple m : meeples) {
//            types.add(m.getClass());
//        }
//        return types;
//    }

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

    public static String getLocalizedNamefor (Class<? extends Feature> feature) {
        try {
            Method m = feature.getMethod("name");
            return (String) m.invoke(null);
        } catch (Exception e) {
            return feature.getSimpleName();
        }
    }

}
