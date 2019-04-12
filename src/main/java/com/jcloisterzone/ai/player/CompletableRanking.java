package com.jcloisterzone.ai.player;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class CompletableRanking {

    private final Completable feature;
    private final int incompletePoints, completePoints;
    private Set<Player> owners;
    private int ownersPower;
    private HashMap<Player, Integer> powers;

    /** probability to complete feature */
    private double probability;

    public CompletableRanking(GameState state, Completable feature) {
        this.feature = feature;
        incompletePoints = feature.getStructurePoints(state, false);
        completePoints = feature.getStructurePoints(state, true);
        powers = feature.getPowers(state).mapValues(Tuple2::_1);

        // copy from Scoreable interface
        ownersPower = powers.values().max().getOrElse(0);
        //can be 0 for Mayor on city without pennant, then return no owners
        if (ownersPower == 0) {
            owners = HashSet.empty();
        } else {
            owners = powers.keySet().filter(p -> powers.get(p).get() == ownersPower);
        }
    }

    public Completable getFeature() {
        return feature;
    }

    public int getCompletePoints() {
        return completePoints;
    }

    public int getIncompletePoints() {
        return incompletePoints;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public Set<Player> getOwners() {
        return owners;
    }

    public int getOwnersPower() {
        return ownersPower;
    }

    public void setOwners(Set<Player> owners) {
        this.owners = owners;
    }

    public HashMap<Player, Integer> getPowers() {
        return powers;
    }

    public void setPowers(HashMap<Player, Integer> powers) {
        this.powers = powers;
    }
}
