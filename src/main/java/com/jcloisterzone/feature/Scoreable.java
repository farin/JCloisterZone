package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Set;

public interface Scoreable extends Structure {

    PointCategory getPointCategory();

    Set<Player> getOwners(GameState state);

    Follower getSampleFollower(GameState state, Player player);

    // getPoints is not present on this interface because of different nature
    // of Completable, Farm and Castle scoring
}
