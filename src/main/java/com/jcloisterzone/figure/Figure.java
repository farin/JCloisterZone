package com.jcloisterzone.figure;

import java.io.Serializable;
import java.util.Objects;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.game.state.GameState;

@Immutable
public abstract class Figure<T extends BoardPointer> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    public Figure(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract T getDeployment(GameState state);

    public Feature getFeature(GameState state) {
        T at = getDeployment(state);
        FeaturePointer fp = at == null ? null : at.asFeaturePointer();
        if (fp == null) {
            return null;
        }
        return state.getFeatureMap().get(fp).getOrNull();
    }

    public Location getLocation(GameState state) {
        T at = getDeployment(state);
        FeaturePointer fp = at == null ? null : at.asFeaturePointer();
        return fp == null ? null : fp.getLocation();
    }

    public Position getPosition(GameState state) {
        T at = getDeployment(state);
        return at == null ? null : at.getPosition();
    }

    public boolean at(GameState state, Position p) {
        return Objects.equals(p, getPosition(state));
    }

    public boolean at(GameState state, FeaturePointer fp) {
        T at = getDeployment(state);
        return Objects.equals(fp, at == null ? null : at.asFeaturePointer());
    }

    public abstract boolean at(GameState state, Structure feature);

    /** true if meeple is deployed on board */
    public boolean isDeployed(GameState state) {
        return getDeployment(state) != null;
    }

    /**
     * isInSupply is not necessary opposite of isDeployed,
     * mind imprisoned followers
     */
    public boolean isInSupply(GameState state) {
        return !isDeployed(state);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Figure)) return false;
        return id.equals(((Figure<?>)obj).id);
    }

    @Override
    public String toString() {
        return id;
    }
}
