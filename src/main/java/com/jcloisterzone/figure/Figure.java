package com.jcloisterzone.figure;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Figure implements Serializable, Cloneable {

    private static final long serialVersionUID = 3264248810294656662L;

    protected final Game game;
    protected FeaturePointer featurePointer;

    public Figure(Game game) {
        assert game != null;
        this.game = game;
    }

//    public abstract void deploy(FeaturePointer at);
//    public abstract void undeploy();

    public Position getPosition() {
        return featurePointer == null ? null : featurePointer.getPosition();
    }

    public Location getLocation() {
        return featurePointer == null ? null : featurePointer.getLocation();
    }

    public void setFeaturePointer(FeaturePointer featurePointer) {
        this.featurePointer = featurePointer;
    }

    public FeaturePointer getFeaturePointer() {
        return featurePointer;
    }

    public boolean at(Position p) {
        if (featurePointer == null || p == null) return false;
        return p.equals(featurePointer.getPosition());
    }

    public boolean at(FeaturePointer fp) {
        if (featurePointer == null || fp == null) return false;
        //dont use equals to permit use this also with MeeplePointer subclass
        return
            Objects.equal(fp.getLocation(), featurePointer.getLocation()) &&
            Objects.equal(fp.getPosition(), featurePointer.getPosition());
    }

    public boolean at(Feature feature) {
        if (featurePointer == null || feature == null) return false;
        return featurePointer.match(feature);
    }

    /** true if meeple is deploayed on board */
    public boolean isDeployed() {
        return featurePointer != null;
    }

    //deployt is not opprosite of supply, mind prisoned followers
    public boolean isInSupply() {
        return featurePointer == null;
    }

    @Override
    public String toString() {
        if (featurePointer == null) {
            return getClass().getSimpleName();
        } else {
            return getClass().getSimpleName() + featurePointer.toString();
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + (featurePointer == null ? 1 : featurePointer.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!obj.getClass().equals(getClass())) return false;
        return Objects.equal(featurePointer, ((Figure) obj).featurePointer);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
