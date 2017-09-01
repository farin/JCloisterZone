package com.jcloisterzone.game;

import java.io.Serializable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Set;

@Immutable
public abstract class Capability<T> implements Serializable {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());


    @SuppressWarnings("unchecked")
    private Class<? extends Capability<T>> narrowClass() {
        return (Class<? extends Capability<T>>) getClass();
    }

    public final T getModel(GameState state) {
        return state.getCapabilityModel(narrowClass());
    }

    public final GameState updateModel(GameState state, Function<T, T> fn) {
        return state.mapCapabilityModel(narrowClass(), fn);
    }

    public final GameState setModel(GameState state, T model) {
        return state.setCapabilityModel(narrowClass(), model);
    }


    public TileDefinition initTile(GameState state, TileDefinition tile, Element xml) throws RemoveTileException {
        return tile;
    }

    //TODO use only settings state linked from whole state?
    public Feature initFeature(GameState settings, String tileId, Feature feature, Element xml) {
        return feature;
    }

//    public List<Feature> extendFeatures(String tileId) {
//        return List.empty();
//    }

    public String getTileGroup(TileDefinition tile) {
        return null;
    }

    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return List.empty();
    }

    public List<Special> createPlayerSpecialMeeples(Player player, MeepleIdProvider idProvider) {
        return List.empty();
    }

    @Deprecated
    public Set<FeaturePointer> extendFollowOptions(Set<FeaturePointer> locations) {
        return locations;
    }

    public GameState onStartGame(GameState state) {
        return state;
    }

    public GameState onTilePlaced(GameState state) {
        return state;
    }

    public GameState onCompleted(GameState state, HashMap<Completable, Integer> completed) {
        return state;
    }

    public GameState onActionPhaseEntered(GameState state) {
        return state;
    }

    public GameState onTurnCleanUp(GameState state) {
        return state;
    }

    public GameState onTurnPartCleanUp(GameState state) {
        return state;
    }

    public GameState finalScoring(GameState state) {
        return state;
    }

    public boolean isTilePlacementAllowed(GameState state, TileDefinition tile, TilePlacement placement) {
        return true;
    }

    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replace("Capability", "");
    }

}
