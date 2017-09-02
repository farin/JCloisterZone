package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Set;

/**
 * Feature which can be scored.
 *
 * Counterintuitive {@code getPoints()} is not present on the interface because of different nature
 * of {@code Completable}, {@code Farm} and {@code Castle} scoring
 *
 */
public interface Scoreable extends Structure {

    PointCategory getPointCategory();

    Set<Player> getOwners(GameState state);

    Follower getSampleFollower(GameState state, Player player);


}
