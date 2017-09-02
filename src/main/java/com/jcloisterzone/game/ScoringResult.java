package com.jcloisterzone.game;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Scoreable;

import io.vavr.collection.Set;

public interface ScoringResult {

    Scoreable getFeature();
    int getPoints();
    Set<Player> getOwners();

}
