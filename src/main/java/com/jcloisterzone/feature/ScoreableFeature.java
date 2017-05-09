package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public abstract class ScoreableFeature extends TileFeature implements Scoreable {

    private static final long serialVersionUID = 1L;

    public ScoreableFeature(List<FeaturePointer> places) {
        super(places);
    }

    protected int getPower(GameState state, Follower f) {
        return f.getPower(state, this);
    }

    @Override
    public Set<Player> getOwners(GameState state) {
        HashMap<Player, Integer> powers = getFollowers(state)
            .foldLeft(HashMap.<Player, Integer>empty(), (acc, m) -> {
                Player player = m.getPlayer();
                int power = getPower(state, m);
                return acc.put(player, acc.get(player).getOrElse(0) + power);
            });

        Integer maxPower = powers.values().max().getOrElse(0);
        //can be 0 for Mayor on city without pennant, then return no owners
        if (maxPower == 0) {
            return HashSet.empty();
        }
        return powers.keySet()
            .filter(p -> powers.get(p).get() == maxPower);
    }

    @Override
    public Follower getSampleFollower(GameState state, Player player) {
        return getFollowers(state).find(f -> f.getPlayer().equals(player)).getOrNull();
    }

    //helpers

    protected int getMageAndWitchPoints(GameState state, int points) {
        Stream<Special> specials = getSpecialMeeples(state);
        if (!specials.find(Predicates.instanceOf(Mage.class)).isEmpty()) {
            points += getPlaces().size();
        }
        if (!specials.find(Predicates.instanceOf(Witch.class)).isEmpty()) {
            if (points % 2 == 1) points++;
            points /= 2;
        }
        return points;
    }

    protected int getLittleBuildingPoints(GameState state) {
        //if (!state.hasCapability(LittleBuildingsCapability.class)) return 0;
        int points = 0;
        // IMMUTABLE TODO
//        for (Entry<LittleBuilding, Integer> entry : littleBuildings.entrySet()) {
//            if (game.getBooleanValue(CustomRule.BULDINGS_DIFFERENT_VALUE)) {
//                LittleBuilding lb = entry.getKey();
//                switch (lb) {
//                    case SHED: points += entry.getValue(); break;
//                    case HOUSE: points += 2*entry.getValue(); break;
//                    case TOWER: points += 3*entry.getValue(); break;
//                }
//            } else {
//                points += entry.getValue();
//            }
//        }
        return points;
    }
}
