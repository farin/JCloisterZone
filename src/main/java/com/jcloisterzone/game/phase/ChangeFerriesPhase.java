package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.FerriesAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.game.capability.FerriesCapability.FerryToken;
import com.jcloisterzone.game.capability.FerriesCapabilityModel;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.ChangeFerry;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import io.vavr.Tuple2;
import io.vavr.collection.Set;

@RequiredCapability(FerriesCapability.class)
public class ChangeFerriesPhase extends Phase {

    public ChangeFerriesPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        PlacedTile lastPlaced = state.getLastPlaced();
        Position pos = lastPlaced.getPosition();
        FerriesCapabilityModel model =  state.getCapabilityModel(FerriesCapability.class);

        Set<FeaturePointer> ferries = model.getFerries()
            .filter(f -> !f.getPosition().equals(pos));

        Set<FeaturePointer> options = state.getTileFeatures2(pos, Road.class)
            .flatMap(t -> t._2.findNearest(state, new FeaturePointer(pos, t._1), fp -> ferries.find(f -> fp.isPartOf(f)).isDefined()))
            .distinct()
            .filter(ferryPart -> !model.getMovedFerries().containsKey(ferryPart.getPosition()))
            .flatMap(ferryPart -> {
                // map nearest ferry to action options
                // (options for each are other possible ferry locations then current)
                Position ferryPos = ferryPart.getPosition();
                PlacedTile ferryTile = state.getPlacedTile(ferryPos);
                return ferryTile
                    .getTile()
                    .getInitialFeatures()
                    .filter(t -> t._2 instanceof Road)
                    .map(Tuple2::_1)
                    .combinations(2)
                    .map(pair -> pair.reduce(Location::union))
                    .map(loc -> loc.rotateCW(ferryTile.getRotation()))
                    .map(loc -> new FeaturePointer(ferryPos, loc))
                    .filter(fp -> !ferries.contains(fp))
                    .toList();
            })
            .toSet();

        if (options.isEmpty()) {
            return next(state);
        }

        return promote(state.setPlayerActions(
            new ActionsState(state.getTurnPlayer(), new FerriesAction(options), true)
        ));
    }

    @PhaseMessageHandler
    public StepResult handlePlaceToken(GameState state, PlaceTokenMessage msg) {
        Token token = msg.getToken();

        if (token != FerryToken.FERRY) {
            throw new IllegalArgumentException();
        }

        FerriesCapabilityModel model =  state.getCapabilityModel(FerriesCapability.class);

        FeaturePointer newFerry = msg.getPointer().asFeaturePointer();
        Position pos = newFerry.getPosition();
        FeaturePointer oldFerry = model.getFerries().find(f -> f.getPosition().equals(pos)).get();

        state = state.setCapabilityModel(FerriesCapability.class, model.mapMovedFerries(
            mf -> mf.put(pos, new Tuple2<>(oldFerry.getLocation(), newFerry.getLocation()))
        ));
        state = (new ChangeFerry(oldFerry, newFerry)).apply(state);
        state = clearActions(state);
        return enter(state);
    }

}
