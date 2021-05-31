package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.modifier.BooleanAnyModifier;
import com.jcloisterzone.feature.modifier.FeatureModifier;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.VineyardCapability;
import com.jcloisterzone.game.setup.GameElementQuery;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.Tuple2;
import io.vavr.collection.*;

/**
 * Cloister or Shrine
 */
public class Cloister extends TileFeature implements Scoreable, CloisterLike, ModifiedFeature<Cloister> {

    private static final long serialVersionUID = 1L;
    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.CLOISTER));

    public static final BooleanAnyModifier SHRINE = new BooleanAnyModifier("cloister[shrine]", new GameElementQuery("shrine"));
    public static final BooleanAnyModifier MONASTERY = new BooleanAnyModifier("cloister[monastery]", new GameElementQuery("cloister"));
    public static final BooleanAnyModifier CHURCH = new BooleanAnyModifier("cloister[church]", new GameElementQuery("church"));

    private final Map<FeatureModifier<?>, Object> modifiers;

    protected final Set<FeaturePointer> neighboring; //for wagon move

    public Cloister() {
        this(INITIAL_PLACE, HashSet.empty(), HashMap.empty());
    }

    public Cloister(List<FeaturePointer> places, Set<FeaturePointer> neighboring, Map<FeatureModifier<?>, Object> modifiers) {
        super(places);
        this.neighboring = neighboring;
        this.modifiers = modifiers;
    }

    @Override
    public Map<FeatureModifier<?>, Object> getModifiers() {
        return modifiers;
    }

    @Override
    public Cloister setModifiers(Map<FeatureModifier<?>, Object> modifiers) {
        if (this.modifiers == modifiers) return this;
        return new Cloister(places, neighboring, modifiers);
    }

    @Override
    public Cloister setNeighboring(Set<FeaturePointer> neighboring) {
        if (this.neighboring == neighboring) return this;
        return new Cloister(places, neighboring, modifiers);
    }

    @Override
    public Set<FeaturePointer> getNeighboring() {
        return neighboring;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Cloister(placeOnBoardPlaces(pos, rot), placeOnBoardNeighboring(pos, rot), modifiers);
    }

    public boolean isShrine(GameState state) {
        return hasModifier(state, SHRINE);
    }

    public boolean isMonastery(GameState state) {
        return hasModifier(state, MONASTERY);
    }

    public boolean isChurch(GameState state) {
        return hasModifier(state, CHURCH);
    }

    public Stream<Tuple2<Meeple, FeaturePointer>> getMeeplesIncludingMonastery2(GameState state) {
        if (isMonastery(state)) {
            FeaturePointer place = places.get();
            Set<FeaturePointer> fps = HashSet.of(place, new FeaturePointer(place.getPosition(), Location.MONASTERY));
            return Stream.ofAll(state.getDeployedMeeples()).filter(t -> fps.contains(t._2));
        }
        return getMeeples2(state);
    }

    public Stream<Meeple> getMeeplesIncludingMonastery(GameState state) {
        if (isMonastery(state)) {
            return getMeeplesIncludingMonastery2(state).map(Tuple2::_1);
        }
        return getMeeples(state);
    }

    public Stream<Tuple2<Follower, FeaturePointer>> getMonasteryFollowers2(GameState state) {
        FeaturePointer place = getPlace().setLocation(Location.MONASTERY);
        return Stream.ofAll(state.getDeployedMeeples()).filter(t -> t._1 instanceof Follower && t._2.equals(place)).map(t -> t.map1(f -> (Follower) f));
    }

    public HashMap<Player, Integer> getMonasteryPowers(GameState state) {
        return getMonasteryFollowers2(state).foldLeft(HashMap.empty(), (acc, follower2) -> {
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
    public PointsExpression getStructurePoints(GameState state, boolean completed) {
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

        List<ExprItem> exprItems = List.of(
           new ExprItem(adjacent + 1, "tiles", adjacent + 1)
        );

        if (adjacent == 8 && adjacentVineyards > 0) {
            exprItems = exprItems.append(new ExprItem(adjacentVineyards, "vineyards", adjacentVineyards * 3));
        }
        String baseName = isShrine(state) ? "shrine" : "cloister";
        return new PointsExpression(adjacent == 8 ? baseName : baseName + ".incomplete", exprItems);
    }

    public static String name() {
        return "Cloister";
    }

    protected Set<FeaturePointer> placeOnBoardNeighboring(Position pos, Rotation rot) {
        return neighboring.map(fp -> fp.rotateCW(rot).translate(pos));
    }
}
