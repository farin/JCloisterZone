package com.jcloisterzone.game.phase;

import java.util.function.Function;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

@RequiredCapability(CountCapability.class)
public class CocScoringPhase extends AbstractCocScoringPhase {

    public CocScoringPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    protected Map<Location, Function<Feature, Boolean>> getScoredQuarters(GameState state) {
        Map<Location, Function<Feature, Boolean>> quarters;
        quarters = HashMap.of(
            Location.QUARTER_BLACKSMITH, null,
            Location.QUARTER_CASTLE, null,
            Location.QUARTER_CATHEDRAL, null
        );


        if (state.getCapabilities().contains(BarnCapability.class)) {
            java.util.HashSet<Feature> barnInvolvedFarms = new java.util.HashSet<>();

            FeaturePointer placedBarnPtr = state.getCapabilityModel(BarnCapability.class);
            Farm placedBarnFarm = placedBarnPtr == null ? null : (Farm) state.getFeature(placedBarnPtr);
            if (placedBarnFarm != null) {
                barnInvolvedFarms.add(placedBarnFarm);

            }

            PlacedTile lastPlaced = state.getLastPlaced();
            Position pos = lastPlaced.getPosition();
            state.getTileFeatures2(pos)
                .map(Tuple2::_2)
                .filter(f -> f != placedBarnFarm)
                .filter(Predicates.instanceOf(Farm.class))
                .map(f -> (Farm) f)
                .filter(farm -> farm.getSpecialMeeples(state)
                    .find(Predicates.instanceOf(Barn.class))
                    .isDefined()
                )
                .filter(farm -> !farm.getFollowers(state).isEmpty())  //must contains at least one follower
                .forEach(farm -> barnInvolvedFarms.add(farm));

            if (!barnInvolvedFarms.isEmpty()) {
                quarters = quarters.put(Location.QUARTER_MARKET, f -> barnInvolvedFarms.contains(f));
            }

        }

        return quarters;
    }

}
