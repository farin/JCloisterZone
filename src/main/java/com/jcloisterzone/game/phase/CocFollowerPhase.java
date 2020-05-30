package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Quarter;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

@RequiredCapability(CountCapability.class)
public class CocFollowerPhase extends Phase {

    public CocFollowerPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        Stream<ScoreEvent> events = Stream.ofAll(state.getCurrentTurnPartEvents())
            .filter(Predicates.instanceOf(ScoreEvent.class))
            .map(ev -> (ScoreEvent) ev)
            .filter(ev -> ev.getCategory().hasLandscapeSource())
            .filter(ev -> ev.getPoints() > 0);

        Player player = state.getTurnPlayer();
        boolean didReceived = false;
        boolean didCauseOpponentScoring = false;
        for (ScoreEvent ev : events) {
            if (ev.getReceiver().equals(player)) {
                didReceived = true;
                break;
            } else {
                didCauseOpponentScoring = true;
            }
        }

        if (didReceived || !didCauseOpponentScoring) {
            return next(state);
        }

        Position quarterPos = state.getCapabilityModel(CountCapability.class).getQuarterPosition();

        Vector<Class<? extends Meeple>> meepleTypes = Vector.of(
            SmallFollower.class, BigFollower.class, Phantom.class,
            Wagon.class, Mayor.class
        );
        Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);
        boolean marketAllowed = state.getBooleanValue(Rule.FARMERS);
        Stream<Tuple2<FeaturePointer, Quarter>> quarters = state.getTileFeatures2(quarterPos)
            .filter(t -> t._1.isCityOfCarcassonneQuarter() && (marketAllowed || t._1 != Location.QUARTER_MARKET))
            .map(t -> new Tuple2<>(new FeaturePointer(quarterPos, t._1), (Quarter) t._2));

        GameState _state = state;
        Vector<PlayerAction<?>> actions = availMeeples.map(meeple -> {
            Set<FeaturePointer> locations = quarters
                .filter(t -> meeple.isDeploymentAllowed(_state, t._1, t._2) == DeploymentCheckResult.OK)
                .map(t -> t._1)
                .toSet();

            PlayerAction<?> action = new MeepleAction(meeple, locations);
            return action;
        });

        actions = actions.filter(action -> !action.isEmpty());

        if (actions.isEmpty()) {
            return next(state);
        }

        state = state.setPlayerActions(new ActionsState(player, actions, true));
        return promote(state);
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());

        if (!fp.getLocation().isCityOfCarcassonneQuarter()) {
            throw new IllegalArgumentException("Only deplpy to the City of Carcassone is allowed");
        }

        state = (new DeployMeeple(m, fp)).apply(state);
        state = clearActions(state);
        return next(state, CocCountPhase.class);
    }
}