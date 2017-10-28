package com.jcloisterzone.game.phase;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.EscapeAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;

import io.vavr.collection.Set;
import io.vavr.collection.Stream;


@RequiredCapability(SiegeCapability.class)
public class EscapePhase extends Phase {

    public EscapePhase(Random random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        Player player = state.getTurnPlayer();
        Stream<City> cities = state.getFeatures(City.class)
            .filter(c -> c.isBesieged())
            .filter(c -> c.isOccupiedBy(state, player));

        Function<City, Stream<MeeplePointer>> getCityFollowers = city -> {
            return city.getFollowers2(state)
                .filter(t -> t._1.getPlayer().equals(player))
                .map(MeeplePointer::new);
        };

        Predicate<Position> cloisterExists = pos -> {
            return state.getFeature(new FeaturePointer(pos, Location.CLOISTER)) != null;
        };

        Set<MeeplePointer> options;

        options = cities
            .filter(c -> {
                Stream<PlacedTile> cityTiles = Stream.ofAll(c.getTilePositions()).map(state::getPlacedTile);

                if (!state.getBooleanValue(Rule.ESCAPE_RGG)) {
                    cityTiles = cityTiles.filter(pt ->
                        pt.getTile().getTrigger() == TileTrigger.BESIEGED
                    );
                }

                Stream<PlacedTile> adjacent = cityTiles
                    .map(PlacedTile::getPosition)
                    .flatMap(state::getAdjacentAndDiagonalTiles);

                return Stream.concat(cityTiles, adjacent)
                    .distinct()
                    .map(PlacedTile::getPosition)
                    .find(cloisterExists)
                    .isDefined();
            })
            .flatMap(getCityFollowers)
            .toSet();

        if (options.isEmpty()) {
            return next(state);
        }

        return promote(state.setPlayerActions(
            new ActionsState(player, new EscapeAction(options), true)
        ));
    }

    @PhaseMessageHandler
    public StepResult handleReturnMeeple(GameState state, ReturnMeepleMessage msg) {
        MeeplePointer ptr = msg.getPointer();

        Meeple meeple = state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));

        switch (msg.getSource()) {
        case SIEGE_ESCAPE:
            EscapeAction princessAction = (EscapeAction) state.getAction();
            if (!princessAction.getOptions().contains(ptr)) {
                throw new IllegalArgumentException("Pointer doesn't match action");
            }
            break;
        default:
            throw new IllegalArgumentException("Return meeple is not allowed");
        }

        state = (new UndeployMeeple(meeple)).apply(state);
        state = clearActions(state);
        return next(state);
    }
}
