package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleReturned;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.MonasteriesCapability;
import com.jcloisterzone.game.capability.RussianPromosTrapCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.CommitMessage;
import com.jcloisterzone.io.message.DeployMeepleMessage;
import com.jcloisterzone.io.message.PassMessage;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.DeployMeeple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class WagonPhase extends Phase {

    public WagonPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        Queue<Tuple2<Wagon, FeaturePointer>> model = state.getCapabilityModel(WagonCapability.class);
        while (!model.isEmpty()) {
            Tuple2<Tuple2<Wagon, FeaturePointer>, Queue<Tuple2<Wagon, FeaturePointer>>> dequeueTuple = model.dequeue();
            model = dequeueTuple._2;
            state = state.setCapabilityModel(WagonCapability.class, model);
            Tuple2<Wagon, FeaturePointer> item = dequeueTuple._1;
            Wagon wagon = item._1;

            Feature feature = state.getFeature(item._2);
            if (feature instanceof Completable) { // skip Castle
                GameState _state = state;
                Set<FeaturePointer> options = getAdjacentFeatures(state, (Completable)feature, item._2)
                        .filter(t -> {
                            Structure f = t._2;
                            if (f instanceof Castle) {
                                // empty castle is considered completed and occupied exclude wagon too
                                return false;
                            }
                            if (f instanceof Completable) {
                                Completable nei = (Completable) f;
                                if (wagon.isDeploymentAllowed(_state, t._1, f) != DeploymentCheckResult.OK) {
                                    return false;
                                }
                                if (nei.isCompleted(_state)) {
                                    return false;
                                }
                                if (nei instanceof Monastery) {
                                    return ((Monastery) nei).getMeeplesIncludingMonastery(_state).isEmpty();
                                }

                                if (nei instanceof Road) {
                                    Road road = (Road) nei;
                                    if (road.isLabyrinth(_state)) {
                                        // current tile mustn't be labyrinth center - apply regular occupation rule to it
                                        Road initial = (Road) _state.getPlacedTile(t._1.getPosition()).getInitialFeaturePartOf(t._1.getLocation())._2;
                                        if (!initial.isLabyrinth(_state)) {
                                            // find if there is empty labyrinth segment
                                            Set<FeaturePointer> segment = road.findSegmentBorderedBy(_state, t._1,
                                                    fp -> {
                                                        Road r = (Road) _state.getPlacedTile(fp.getPosition()).getInitialFeaturePartOf(fp.getLocation())._2;
                                                        return r.isLabyrinth(_state);
                                                    }).toSet();
                                            boolean segmentIsEmpty = Stream.ofAll(_state.getDeployedMeeples())
                                                    .filter(x -> segment.contains(x._2))
                                                    .isEmpty();
                                            if (segmentIsEmpty) {
                                                // whole road is occupied but segment divided by labyrinth is free
                                                return true;
                                            }
                                        }
                                    }
                                }

                                return !nei.isOccupied(_state);
                            }
                            return false; // eg f == null
                        })
                        .flatMap(t -> {
                            Structure struct = t._2;
                            if (struct instanceof Monastery && ((Monastery)struct).isSpecialMonastery(_state) && _state.hasCapability(MonasteriesCapability.class)) {
                                return List.of(t, new Tuple2<>(new FeaturePointer(t._1.getPosition(), Monastery.class, Location.AS_ABBOT), struct));
                            }
                            return List.of(t);
                        })
                        .map(Tuple2::_1)
                        .filter(fp -> {
                            for (Capability<?> cap : _state.getCapabilities().toSeq()) {
                                if (!cap.isMeepleDeploymentAllowed(_state, fp.getPosition())) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .toSet();

                if (!options.isEmpty()) {
                    MeepleReturned returnedEvent = (MeepleReturned) state.getEvents().findLast(ev -> ev instanceof MeepleReturned && ((MeepleReturned) ev).getMeeple() == wagon).getOrNull();
                    PlayerAction<?> action = new MeepleAction(wagon, options, returnedEvent.getFrom());
                    state = state.setPlayerActions(
                        new ActionsState(wagon.getPlayer(), action, true)
                    );
                    return promote(state);
                }
            }
        }
        return next(state);
    }

    private Stream<Tuple2<FeaturePointer, Structure>> getAdjacentFeatures(GameState state, Completable feature, FeaturePointer source) {
        if ("C1".equals(state.getStringRule(Rule.WAGON_MOVE))) {
            return Stream.ofAll(feature.getNeighboring())
                    .map(fp -> new Tuple2<>(fp, (Structure) state.getFeature(fp)));
        } else {
            Position sourcePos = source.getPosition();
            return Stream.ofAll(Position.ADJACENT_AND_DIAGONAL.values())
                    .map(p -> sourcePos.add(p))
                    .append(sourcePos)
                    .flatMap(pos -> state.getTileFeatures2(pos, Structure.class));
        }
    }


    @PhaseMessageHandler
    @Override
    public StepResult handlePass(GameState state, PassMessage msg) {
        if (!state.getPlayerActions().isPassAllowed()) {
            throw new IllegalStateException("Pass is not allowed");
        }

        state = clearActions(state);
        return enter(state);
    }

    @PhaseMessageHandler
    public StepResult handleCommit(GameState state, CommitMessage msg) {
        RussianPromosTrapCapability russianPromos = state.getCapabilities().get(RussianPromosTrapCapability.class);
        if (russianPromos != null) {
            state = russianPromos.trapFollowers(state);
        }

        state = clearActions(state);
        return enter(state);
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Player player = state.getActivePlayer();
        Meeple m = player.getMeepleFromSupply(state, msg.getMeepleId());
        if (!(m instanceof Wagon)) {
            throw new IllegalArgumentException("Invalid follower");
        }

        MeepleAction action = (MeepleAction) state.getPlayerActions().getActions().find(a -> a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(Wagon.class)).get();
        if (action.getOptions().find(p -> fp.equals(p)).isEmpty()) {
            throw new IllegalArgumentException("Invalid placement");
        }

        state = (new DeployMeeple(m, fp)).apply(state);

        Queue<Tuple2<Wagon, FeaturePointer>> model = state.getCapabilityModel(WagonCapability.class);
        if (model.find(t -> t._1.getPlayer().equals(player)).isEmpty()) {
            // player has no other wagon to deploy (can happen only with multiple wagons setup)
            return promote(state.setPlayerActions(new ActionsState(player, new ConfirmAction(), false)));
        } else {
            state = clearActions(state);
        }

        return enter(state);
    }
}
