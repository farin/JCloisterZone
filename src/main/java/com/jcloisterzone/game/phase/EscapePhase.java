package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.UndeployMeeple;
import com.jcloisterzone.io.message.ReturnMeepleMessage;
import com.jcloisterzone.io.message.ReturnMeepleMessage.ReturnMeepleSource;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

import java.util.function.Function;
import java.util.function.Predicate;


public class EscapePhase extends Phase {

    public EscapePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
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

                if ("siege-tile".equals(state.getStringRule(Rule.ESCAPE_VARIANT))) {
                    cityTiles = cityTiles.filter(pt ->
                        pt.getTile().hasModifier(SiegeCapability.SIEGE_ESCAPE_TILE)
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
            new ActionsState(player, new ReturnMeepleAction(options, ReturnMeepleSource.SIEGE_ESCAPE), true)
        ));
    }

    @PhaseMessageHandler
    public StepResult handleReturnMeeple(GameState state, ReturnMeepleMessage msg) {
        MeeplePointer ptr = msg.getPointer();

        Meeple meeple = state.getDeployedMeeples().find(m -> ptr.match(m._1)).map(t -> t._1)
            .getOrElseThrow(() -> new IllegalArgumentException("Pointer doesn't match any meeple"));

        if (msg.getSource() != ReturnMeepleSource.SIEGE_ESCAPE) {
            throw new IllegalArgumentException("Return meeple is not allowed");
        }

        ReturnMeepleAction escapeAction = (ReturnMeepleAction) state.getAction();
        assert escapeAction.getSource() == ReturnMeepleSource.SIEGE_ESCAPE;
        if (!escapeAction.getOptions().contains(ptr)) {
            throw new IllegalArgumentException("Pointer doesn't match action");
        }

        state = (new UndeployMeeple(meeple, true)).apply(state);
        state = clearActions(state);
        return next(state);
    }
}
