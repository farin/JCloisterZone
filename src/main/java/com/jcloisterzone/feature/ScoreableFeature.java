package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
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
        Map<Position, Token> buildings = state.getCapabilityModel(LittleBuildingsCapability.class);
        if (buildings == null) {
            return 0;
        }
        Set<Position> position = getTilePositions();
        buildings = buildings.filterKeys(pos -> position.contains(pos));

        if (state.getBooleanValue(CustomRule.BULDINGS_DIFFERENT_VALUE)) {
            return buildings
                .values()
                .map(token -> {
                    if (token == Token.LB_SHED) return 1;
                    if (token == Token.LB_HOUSE) return 2;
                    if (token == Token.LB_TOWER) return 3;
                    throw new IllegalArgumentException();
                })
                .sum().intValue();
        } else {
            return buildings.size();
        }
    }
}
