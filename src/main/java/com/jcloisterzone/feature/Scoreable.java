package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
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

    default HashMap<Player, Integer> getPowers(GameState state) {
        return getFollowers(state)
            .foldLeft(HashMap.<Player, Integer>empty(), (acc, m) -> {
                Player player = m.getPlayer();
                int power = m.getPower(state, this);
                return acc.put(player, acc.get(player).getOrElse(0) + power);
            });
    }

    default Set<Player> getOwners(GameState state) {
        HashMap<Player, Integer> powers = getPowers(state);
        Integer maxPower = powers.values().max().getOrElse(0);
        //can be 0 for Mayor on city without pennant, then return no owners
        if (maxPower == 0) {
            return HashSet.empty();
        }
        return powers.keySet().filter(p -> powers.get(p).get() == maxPower);
    }


    default Follower getSampleFollower(GameState state, Player player) {
        return getFollowers(state).find(f -> f.getPlayer().equals(player)).getOrNull();
    }

    default int getLittleBuildingPoints(GameState state) {
        Map<Position, LittleBuilding> buildings = state.getCapabilityModel(LittleBuildingsCapability.class);
        if (buildings == null) {
            return 0;
        }
        Set<Position> position = getTilePositions();
        Seq<LittleBuilding> buldingsSeq = buildings.filterKeys(pos -> position.contains(pos)).values();

        return LittleBuildingsCapability.getBuildingsPoints(state, buldingsSeq);
    }
}
