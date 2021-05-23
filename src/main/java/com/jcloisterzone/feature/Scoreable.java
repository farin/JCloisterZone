package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.HillCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability.LittleBuilding;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.*;

/**
 * Feature which can be scored.
 *
 * Counterintuitive {@code getPoints()} is not present on the interface because of different nature
 * of {@code Completable}, {@code Farm} and {@code Castle} scoring
 *
 */
public interface Scoreable extends Structure {

    /**
     * Map value is int tuple with power and tie breaking power
     * (hill presence/hill count according to rules)
     */
    default HashMap<Player, Tuple2<Integer, Integer>> getPowers(GameState state) {
    	boolean useHillTiebreaker = state.hasCapability(HillCapability.class);
    	boolean useOnHillCount = "number-of-followers".equals(state.getStringRule(Rule.HILL_TIEBREAKER));
        return getFollowers2(state)
            .foldLeft(HashMap.empty(), (acc, follower2) -> {
            	Follower follower = follower2._1;
            	FeaturePointer fp = follower2._2;
                Player player = follower.getPlayer();
                int power = follower.getPower(state, this);
                Tuple2<Integer, Integer> t = acc.get(player).getOrElse(new Tuple2<Integer, Integer>(0, 0));
                t = t.map1(p -> p + power);
                if (useHillTiebreaker) {
                	boolean onHill = state.getPlacedTile(fp.getPosition()).getTile().hasModifier(HillCapability.HILL);
                	if (onHill) {
                		if (useOnHillCount) {
                			t = t.map2(cnt -> cnt + 1);
                		} else {
                			if (t._2 == 0) {
                				t = t.update2(1);
                			}
                		}
                	}
                }
                return acc.put(player, t);
            });
    }

    default Set<Player> getOwners(GameState state) {
        HashMap<Player, Tuple2<Integer, Integer>> powers = getPowers(state);
        int maxPower = powers.values().map(Tuple2::_1).max().getOrElse(0);
        //can be 0 for Mayor on city without pennant, then return no owners
        if (maxPower == 0) {
            return HashSet.empty();
        }
        int maxTiebreaker = powers.values().filter(t -> t._1 == maxPower).map(Tuple2::_2).max().getOrElse(0);
        return powers.filterValues(t -> t._1 == maxPower && t._2 == maxTiebreaker).keySet();
    }

    default Follower getSampleFollower(GameState state, Player player) {
        return getFollowers(state).find(f -> f.getPlayer().equals(player)).getOrNull();
    }

    default Tuple2<Follower, FeaturePointer> getSampleFollower2(GameState state, Player player) {
        return getFollowers2(state).find(t -> t._1.getPlayer().equals(player)).getOrNull();
    }

    default List<ExprItem> getLittleBuildingPoints(GameState state) {
        Map<Position, LittleBuilding> buildings = state.getCapabilityModel(LittleBuildingsCapability.class);
        if (buildings == null) {
            return List.empty();
        }
        Set<Position> position = getTilePositions();
        Seq<LittleBuilding> buldingsSeq = buildings.filterKeys(pos -> position.contains(pos)).values();

        return LittleBuildingsCapability.getBuildingsPoints(state, buldingsSeq);
    }
}
