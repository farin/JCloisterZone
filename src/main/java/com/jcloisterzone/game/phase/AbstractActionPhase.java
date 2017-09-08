package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.play.FlierRollEvent;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.FlyingMachine;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.DeployMeeple;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;

public abstract class AbstractActionPhase extends Phase {

    public AbstractActionPhase(Config config, Random random) {
        super(config, random);
    }

    private boolean isMeepleDeploymentAllowedByCapabilities(GameState state, Position pos) {
        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            if (!cap.isMeepleDeploymentAllowed(state, pos)) {
                return false;
            }
        }
        return true;
    }

    protected Vector<PlayerAction<?>> prepareMeepleActions(GameState state, Vector<Class<? extends Meeple>> meepleTypes) {
        Player player = state.getTurnPlayer();
        Vector<Meeple> availMeeples = player.getMeeplesFromSupply(state, meepleTypes);

        PlacedTile lastPlaced = state.getLastPlaced();
        Position currentTilePos = lastPlaced.getPosition();
        Stream<PlacedTile> tiles;

        if (lastPlaced.getTile().getTrigger() == TileTrigger.PORTAL && !state.getFlags().contains(Flag.PORTAL_USED)) {
            tiles = Stream.ofAll(state.getPlacedTiles().values());
        } else {
            tiles = Stream.of(lastPlaced);
        }

        Stream<Tuple2<FeaturePointer, Structure>> placesFp = tiles.flatMap(tile -> {
            Position pos = tile.getPosition();
            boolean isCurrentTile = pos.equals(currentTilePos);

            if (!isMeepleDeploymentAllowedByCapabilities(state, pos)) {
                return Stream.empty();
            }

            Stream<Tuple2<Location, Structure>> places = state.getTileFeatures2(pos, Structure.class);

            if (!isCurrentTile) {
                //exclude completed
                places = places.filter(t -> !(t._2 instanceof Completable) || ((Completable)t._2).isOpen(state));
            }

            if (state.hasFlag(Flag.FLYING_MACHINE_USED)) {
                places = places.filter(t -> t._1 != Location.FLYING_MACHINE);
            }

            return places.map(t -> t.map1(loc -> new FeaturePointer(pos, loc)));
        });

        Vector<PlayerAction<?>> actions = availMeeples.map(meeple -> {
            Set<FeaturePointer> locations = placesFp
                .filter(t -> !t._2.isOccupied(state))
                .filter(t -> meeple.isDeploymentAllowed(state, t._1, t._2) == DeploymentCheckResult.OK)
                .flatMap(t -> {
                    if (t._2 instanceof Cloister && ((Cloister)t._2).isMonastery()) {
                        return List.of(
                            t,
                            t.update1(new FeaturePointer(t._1.getPosition(), Location.MONASTERY))
                        );
                    } else {
                        return List.of(t);
                    }
                })
                .map(t -> t._1)
                .toSet();

            PlayerAction<?> action = new MeepleAction(meeple, locations);
            return action;
        });

        return actions.filter(action -> !action.isEmpty());
    }

    @Override
    @PhaseMessageHandler
    public StepResult handlePayRansom(GameState state, PayRansomMessage msg) {
        state = (new PayRansom(msg.getMeepleId())).apply(state);
        return enter(state); //recompute actions with returned followers
    }

    @PhaseMessageHandler
    public StepResult handleDeployMeeple(GameState state, DeployMeepleMessage msg) {
        FeaturePointer fp = msg.getPointer();
        Meeple m = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());
        PlacedTile placedTile = state.getLastPlaced();

        //TODO validate placement against players actions

        if (fp.getLocation() != Location.TOWER
            && placedTile.getTile().getTrigger() == TileTrigger.PORTAL
            && !fp.getPosition().equals(placedTile.getPosition())
        ) {
            state = state.addFlag(Flag.PORTAL_USED);
        }

        state = (new DeployMeeple(m, fp)).apply(state);
        if (m instanceof Barn) {
            state = state.setCapabilityModel(BarnCapability.class, fp);
        }

        state = clearActions(state);
        return next(state);
    }

    private Position getTargetPosition(Position pos, Location direction, int distance) {
        for (int i = 0; i < distance; i++) {
            pos = pos.add(direction);
        }
        return pos;
    }

    @PhaseMessageHandler
    public StepResult handleDeployFlier(GameState state, DeployFlierMessage msg) {
        PlacedTile placedTile = state.getLastPlaced();
        FlyingMachine flyingMachine = (FlyingMachine) state.getFeature(msg.getPointer());
        Meeple meeple = state.getActivePlayer().getMeepleFromSupply(state, msg.getMeepleId());

        int distance = getRandom().nextInt(3) + 1;
        state = state.addFlag(Flag.FLYING_MACHINE_USED);
        state = state.appendEvent(new FlierRollEvent(
            PlayEventMeta.createWithActivePlayer(state), placedTile.getPosition(), distance)
        );

        Position target = getTargetPosition(placedTile.getPosition(), flyingMachine.getDirection(), distance);
        PlacedTile targetTile = state.getPlacedTile(target);
        if (targetTile == null || !isMeepleDeploymentAllowedByCapabilities(state, target)) {
            // empty place on landing tile
            return next(state);
        }

        GameState _state = state;
        Set<FeaturePointer> options = state.getTileFeatures2(target, Completable.class)
            .filter(t -> t._2.isOpen(_state))
            .filter(t -> meeple.isDeploymentAllowed(_state, new FeaturePointer(target, t._1), t._2) == DeploymentCheckResult.OK)
            .filter(t -> t._1 != Location.FLYING_MACHINE) // no chained flier
            .map(t -> new FeaturePointer(target, t._1))
            .toSet();

        if (options.isEmpty()) {
            return next(state);
        }

        PlayerAction<?> action = new MeepleAction(meeple, options);
        state = state.setPlayerActions(new ActionsState(state.getTurnPlayer(), action, false));
        return promote(state);
    }
}
