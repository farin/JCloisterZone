package com.jcloisterzone;

import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Predicates;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Represents a player.
 */
@Immutable
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    final private int index;

    /**
     * Instantiates a new Player.
     * @param index the index of the player
     */
    public Player(int index) {
        this.index = index;
    }

    /**
     * Gets the index of the player.
     *
     * @return the index of the player
     */
    public int getIndex() {
        return index;
    }


    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Player) {
            return index == ((Player)o).index;
        }
        return false;
    }

    /**
     * Gets the next player.
     *
     * @param state the state of the game
     * @return the next player
     */
    public Player getNextPlayer(GameState state) {
        int nextPlayerIndex = (index + 1) % state.getPlayers().length();
        return state.getPlayers().getPlayer(nextPlayerIndex);
    }

    /**
     * Gets the previous player.
     *
     * @param state the state of the game
     * @return the previous player
     */
    public Player getPrevPlayer(GameState state) {
        // mod (% operator) on negative numbers is also negative! don't use it for prev player
        int prevPlayerIndex = index == 0 ? state.getPlayers().length() - 1 : index - 1;
        return state.getPlayers().getPlayer(prevPlayerIndex);
    }

    /**
     * Gets the current score of the player.
     *
     * @param state the state of the game
     * @return the score of the player
     */
    public int getPoints(GameState state) {
        return state.getPlayers().getScore().get(index);
    }


    /**
     * Gets the followers of the player.
     *
     * @param state the state of the game
     * @return the followers of the player
     */
    public Seq<Follower> getFollowers(GameState state) {
        return state.getPlayers().getFollowers().get(index);
    }

    /**
     * Gets the special meeples of the player.
     *
     * @param state the state of the game
     * @return the special meeples of the player
     */
    public Seq<Special> getSpecialMeeples(GameState state) {
        return state.getPlayers().getSpecialMeeples().get(index);
    }

    /**
     * Gets the meeples of the player.
     *
     * @param state the state of the game
     * @return the meeples of the player
     */
    public Stream<Meeple> getMeeples(GameState state) {
        return Stream.concat(getFollowers(state), getSpecialMeeples(state));
    }

    /**
     * Checks whether the player has any special meeple of a specific class.
     *
     * @param state the state of the game
     * @param clazz the class of interest
     * @return {@code true} if the player has special meeples of the given class, {@code otherwise}
     */
    public boolean hasSpecialMeeple(GameState state, Class<? extends Special> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        return !Stream.ofAll(getSpecialMeeples(state))
            .filter(m -> m.getClass().equals(clazz))
            .filter(m -> m.isInSupply(state))
            .isEmpty();
    }

    /**
     * Checks if the player has any followers.
     *
     * @param state the state of the game
     * @return {@code true} if the player has followers, {@code otherwise}
     */
    public boolean hasFollower(GameState state) {
        return !Stream.ofAll(getFollowers(state))
            .filter(m -> m.isInSupply(state))
            .isEmpty();
    }

    /**
     * Checks whether the player has any follower of a specific class.
     *
     * @param state the state of the game
     * @param clazz the class of interest
     * @return {@code true} if the player has followers of the given class, {@code otherwise}
     */
    public boolean hasFollower(GameState state, Class<? extends Follower> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        //check equality not instanceOf - phantom is subclass of small follower
        return !Stream.ofAll(getFollowers(state))
                .filter(m -> m.getClass().equals(clazz))
                .filter(m -> m.isInSupply(state))
                .isEmpty();
    }

    /**
     * Gets a meeple from the player supply.
     *
     * @param <T>   the type of meeple
     * @param state the state of the game
     * @param clazz the class of the desired meeple
     * @return a meeple from the player supply
     */
    @SuppressWarnings("unchecked")
    public <T extends Meeple> T getMeepleFromSupply(GameState state, Class<T> clazz) {
        assert !Modifier.isAbstract(clazz.getModifiers());
        Seq<? extends Meeple> collection = (Follower.class.isAssignableFrom(clazz) ? getFollowers(state) : getSpecialMeeples(state));
        return (T) Stream.ofAll(collection)
            .filter(m -> m.getClass().equals(clazz))
            .find(m -> m.isInSupply(state))
            .getOrNull();
    }

    /**
     * Gets a meeple from the player supply given its id.
     *
     * @param state    the state of the game
     * @param meepleId the meeple id
     * @return the meeple from the player supply
     */
    public Meeple getMeepleFromSupply(GameState state, String meepleId) {
        return Stream.ofAll(getMeeples(state))
            .find(m -> m.getId().equals(meepleId))
            .filter(m -> m.isInSupply(state))
            .getOrNull();
    }

    /**
     * Gets meeples of desired types from the player supply.
     *
     * @param state       the state of the game
     * @param meepleTypes the meeple types desired
     * @return the meeples from the player supply
     */
    public Vector<Meeple> getMeeplesFromSupply(GameState state, Vector<Class<? extends Meeple>> meepleTypes) {
        return meepleTypes.map(cls -> (Meeple) getMeepleFromSupply(state, cls))
            .filter(Predicates.isNotNull());
    }

    @Override
    public String toString() {
        return "Player " + index;
    }
}
