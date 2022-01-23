package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.FerriesAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.FerriesCapability;
import com.jcloisterzone.game.capability.FerriesCapability.FerryToken;
import com.jcloisterzone.game.capability.RussianPromosTrapCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.PlaceFerry;
import io.vavr.collection.Set;

public class PlaceFerryPhase extends Phase {

    public PlaceFerryPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();
        Tile tile = placedTile.getTile();
        Position pos = placedTile.getPosition();
        Rotation rot = placedTile.getRotation();
        if (tile.hasModifier(FerriesCapability.LAKE_FERRY)) {
            Set<FeaturePointer> ferries = tile.getInitialFeatures()
                .filter(t -> t._2 instanceof Road)
                .map(t -> t._1.getLocation())
                .combinations(2)
                .map(pair -> pair.reduce(Location::union))
                .map(loc -> new FeaturePointer(pos, Road.class, loc.rotateCW(rot)))
                .toSet();

            return promote(state.setPlayerActions(
                new ActionsState(state.getTurnPlayer(), new FerriesAction(ferries), false)
            ));
        }
        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceToken(GameState state, PlaceTokenMessage msg) {
        Token token = msg.getToken();

        if (token != FerryToken.FERRY) {
            throw new IllegalArgumentException();
        }

        FeaturePointer ferry = msg.getPointer().asFeaturePointer();
        state = (new PlaceFerry(ferry)).apply(state);
        state = clearActions(state);

        RussianPromosTrapCapability russianPromos = state.getCapabilities().get(RussianPromosTrapCapability.class);
        if (russianPromos != null) {
            state = russianPromos.trapFollowers(state);
        }

        return next(state);
    }
}
