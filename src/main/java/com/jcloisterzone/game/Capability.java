package com.jcloisterzone.game;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.ModifiedFeature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.modifier.BooleanModifier;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.function.Function;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

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

    public GameState onStartGame(GameState state) {
        return state;
    }

    public GameState onTilePlaced(GameState state, PlacedTile placedTile) {
        return state;
    }

    public List<ReceivedPoints> appendFiguresBonusPoints(GameState state, List<ReceivedPoints> bonusPoints, Scoreable feature, boolean isFinal) {
        return bonusPoints;
    }

    /**
     * @param state game state
     * @param completed all Completables (roads, cities, cloisters) and Castles completed this turn
     * */
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
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

    public static Class<? extends Capability<?>> classForName(String name) throws ClassNotFoundException {
        ClassLoader defaultLoader = Capability.class.getClassLoader();
        try {
            return classForName(name, defaultLoader);
        } catch (ClassNotFoundException ex) {
//            for (Plugin p : Client.getInstance().getPlugins()) {
//                if (!p.isEnabled() || p.getLoader().equals(defaultLoader)) {
//                    continue;
//                }
//                try {
//                    return classForName(name, p.getLoader());
//                } catch (ClassNotFoundException nested) {
//                    // do nothing
//                }
//            }
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Capability<?>> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        String clsName = "com.jcloisterzone.game.capability." + name + "Capability";
        return (Class<? extends Capability<?>>) Class.forName(clsName, true, classLoader);
    }

    public static String nameForClass(Class<? extends Capability<?>> cls) {
        return cls.getSimpleName().replaceAll("Capability$", "");
    }
}
