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
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

@Immutable
public abstract class Capability<T> implements Serializable {

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


    /**
     * @param state
     * @param tile
     * @param tileElements XML elements defining tile. Because of tile inheritance, more than one element can exist.
     * @return
     * @throws RemoveTileException
     */
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) throws RemoveTileException {
        return tile;
    }

    public Feature initFeature(GameState settings, String tileId, Feature feature, Element xml) {
        return feature;
    }

    public String getTileGroup(Tile tile) {
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

    public GameState onTilePlaced(GameState state, PlacedTile placedTile) {
        return state;
    }

    /**
     * @param state game state
     * @param completed all Completables (roads, cities, cloisters) and Castles completed this turn
     * */
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoringResult> completed) {
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

    public GameState onFinalScoring(GameState state) {
        return state;
    }

    public boolean isTilePlacementAllowed(GameState state, Tile tile, PlacementOption placement) {
        return true;
    }

    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        return nameForClass((Class<? extends Capability<?>>) getClass());
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Capability<?>> classForName(String name) {
        String clsName = "com.jcloisterzone.game.capability." + name + "Capability";
        try {
            return (Class<? extends Capability<?>>) Class.forName(clsName);
        } catch (ClassNotFoundException e) {
            Logger logger = LoggerFactory.getLogger(Capability.class);
            logger.error("Unable to find capability class.", e);
            return null;
        }
    }

    public static String nameForClass(Class<? extends Capability<?>> cls) {
        return cls.getSimpleName().replaceAll("Capability$", "");
    }

}
