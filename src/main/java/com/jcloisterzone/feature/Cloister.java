package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.HillCapability;
import com.jcloisterzone.game.capability.VineyardCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Tuple2;
import io.vavr.collection.*;

/**
 * Cloister or Shrine
 */
public class Cloister extends TileFeature implements Scoreable, CloisterLike {

    private static final long serialVersionUID = 1L;
    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.CLOISTER));

    protected final Set<FeaturePointer> neighboring; //for wagon move

    protected final boolean shrine; // Cult expansion
    protected final boolean monastery; // Monasteries expansion
    protected final boolean church; // Darmstadt promo expansion

    public Cloister() {
        this(INITIAL_PLACE, HashSet.empty(), false, false, false);
    }

    public Cloister(List<FeaturePointer> places, Set<FeaturePointer> neighboring, boolean shrine, boolean monastery, boolean church) {
        super(places);
        this.neighboring = neighboring;
        this.shrine = shrine;
        this.monastery = monastery;
        this.church = church;
    }

    @Override
    public Cloister setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Cloister(places, neighboring, shrine, monastery, church);
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Cloister(placeOnBoardPlaces(pos, rot), placeOnBoardNeighboring(pos, rot), shrine, monastery, church);
    }

    public boolean isShrine() {
        return shrine;
    }

    public Cloister setShrine(boolean shrine) {
        if (this.shrine == shrine) return this;
        return new Cloister(places, neighboring, shrine, monastery, church);
    }

    public boolean isMonastery() {
        return monastery;
    }

    public Cloister setMonastery(boolean monastery) {
        if (this.monastery == monastery) return this;
        return new Cloister(places, neighboring, shrine, monastery, church);
    }

    public boolean isChurch() {
        return church;
    }

    public Cloister setChurch(boolean church) {
        if (this.church == church) return this;
        return new Cloister(places, neighboring, shrine, monastery, church);
    }

    public Stream<Tuple2<Meeple, FeaturePointer>> getMeeplesIncludingMonastery2(GameState state) {
        if (isMonastery()) {
            FeaturePointer place = places.get();
            Set<FeaturePointer> fps = HashSet.of(place, new FeaturePointer(place.getPosition(), Location.MONASTERY));
            return Stream.ofAll(state.getDeployedMeeples()).filter(t -> fps.contains(t._2));
        }
        return getMeeples2(state);
    }

    public Stream<Meeple> getMeeplesIncludingMonastery(GameState state) {
        if (isMonastery()) {
            return getMeeplesIncludingMonastery2(state).map(Tuple2::_1);
        }
        return getMeeples(state);
    }

    public Stream<Tuple2<Follower, FeaturePointer>> getMonasteryFollowers2(GameState state) {
        FeaturePointer place = getPlace().setLocation(Location.MONASTERY);
        return Stream.ofAll(state.getDeployedMeeples()).filter(t -> t._1 instanceof Follower && t._2.equals(place)).map(t -> t.map1(f -> (Follower) f));
    }

    public HashMap<Player, Integer> getMonasteryPowers(GameState state) {
        return getMonasteryFollowers2(state).foldLeft(HashMap.<Player, Integer>empty(), (acc, follower2) -> {
                Follower follower = follower2._1;
                FeaturePointer fp = follower2._2;
                Player player = follower.getPlayer();
                int power = follower.getPower(state, this);
                Integer p = acc.get(player).getOrElse(0);
                return acc.put(player, p + power);
            });
    }

    public Set<Player> getMonasteryOwners(GameState state) {
        HashMap<Player, Integer> powers = getMonasteryPowers(state);
        int maxPower = powers.values().max().getOrElse(0);
        if (maxPower == 0) {
            return HashSet.empty();
        }
        return powers.filterValues(p -> p == maxPower).keySet();
    }

    public Follower getMonasterySampleFollower(GameState state, Player player) {
        return getMonasteryFollowers2(state).map(Tuple2::_1).find(f -> f.getPlayer().equals(player)).getOrNull();
    }

    @Override
    public int getPoints(GameState state) {
    	boolean scoreVineyards = state.hasCapability(VineyardCapability.class);
        Position p = places.get().getPosition();
        int adjacent = 0;
        int adjacentVineyards = 0;
        for (Tuple2<Location, PlacedTile> t : state.getAdjacentAndDiagonalTiles2(p)) {
        	adjacent++;
        	if (scoreVineyards && t._2.getTile().hasModifier(VineyardCapability.VINEYARD)) {
        		adjacentVineyards++;
        	}
        }
        int vineyardPoints = adjacent == 8 ? adjacentVineyards * 3 : 0;
        return adjacent + 1 + vineyardPoints + getLittleBuildingPoints(state);
    }

    public static String name() {
        return _tr("Cloister");
    }

    protected Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return neighboring.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
